package com.srsj.server;

import com.flazr.io.flv.FlvAtom;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.server.RtmpServer;
import com.flazr.rtmp.server.ServerApplication;
import com.flazr.rtmp.server.ServerStream;
import com.srsj.util.ConstUtil;
import com.srsj.util.RtmpUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpFileServerHandler extends
        SimpleChannelInboundHandler<FullHttpRequest> {
    private Logger logger = LoggerFactory.getLogger(HttpFileServerHandler.class);
    private final String url;

    public HttpFileServerHandler(String url) {
        this.url = url;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
                             FullHttpRequest request) throws Exception {
        if (!request.getDecoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }
        if (request.getMethod() != GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }
        final String uri = request.getUri();
        RtmpUtil.log(logger, ConstUtil.LogLevelEnum.Info, "uri", uri, null);
        String[] uriArray = uri.split("\\?");
        String appStreamName = uriArray.length > 0 ? uriArray[0] : "";
        String paramString = uriArray.length > 1 ? uriArray[1] : "";
        if (StringUtils.isBlank(appStreamName)) {
            sendError(ctx, FORBIDDEN);
            return;
        }
        String[] appStreamNameArray = appStreamName.split("/");
        String appName = appStreamNameArray.length > 1 ? appStreamNameArray[1] : "";
        String streamName = appStreamNameArray.length > 2 ? appStreamNameArray[2] : "";
        if (StringUtils.isBlank(appName)) {
            sendError(ctx, FORBIDDEN);
            return;
        }
        String[] streamNameArray = streamName.split("\\.");
        streamName = streamNameArray.length > 0 ? streamNameArray[0] : "";
        String subfix = streamNameArray.length > 1 ? streamNameArray[1] : "";
        if (StringUtils.isBlank(streamName)) {
            sendError(ctx, FORBIDDEN);
            return;
        }

//        RtmpUtil.channelMap.put(ctx.channel().id(),appStreamName);
//        RtmpServer.CHANNELS.add(ctx.channel());
//        final String path = sanitizeUri(uri);
//        logger.info(path);
//        if (path == null) {
//            sendError(ctx, FORBIDDEN);
//            return;
//        }
//        File file = new File("d:\\dev\\git\\sanshidi\\respo\\dev.flv");
//        if (file.isHidden() || !file.exists()) {
//            sendError(ctx, NOT_FOUND);
//            return;
//        }
//        if (file.isDirectory()) {
//            if (uri.endsWith("/")) {
//                sendListing(ctx, file);
//            } else {
//                sendRedirect(ctx, uri + '/');
//            }
//            return;
//        }
//        if (!file.isFile()) {
//            sendError(ctx, FORBIDDEN);
//            return;
//        }
//        RandomAccessFile randomAccessFile = null;
//        try {
//            randomAccessFile = new RandomAccessFile(file, "r");// 以只读的方式打开文件
//        } catch (FileNotFoundException fnfe) {
//            sendError(ctx, NOT_FOUND);
//            return;
//        }
//        long fileLength = randomAccessFile.length();

        ServerApplication serverApplication = RtmpServer.APPLICATIONS.get(appName);
        if (serverApplication != null) {
            ServerStream serverStream = serverApplication.subscriberGetStream(streamName);
            if (serverStream != null) {
                HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
//        setContentLength(response, fileLength);
//        setContentTypeHeader(response, file);
//        response.headers().set(CONTENT_TYPE, "flv");
                if (isKeepAlive(request)) {
                    response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                }
                response.headers().set("Access-Control-Allow-Origin", "*");
                response.headers().set("Access-Control-Allow-Credentials", true);
                response.headers().set("Content-Type", "video/x-flv");
                response.headers().set("Expires", -1);
                response.headers().set("Server", "netty");
                response.headers().set("Transfer-Encoding", "chunked");
                response.headers().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                response.headers().set("Access-Control-Allow-Headers", "DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization,range");
                // write response header
                ctx.writeAndFlush(response);

                // write chunked stream:flv header
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(ByteBufUtil.getBytes(FlvAtom.flvHeader()));
                ctx.write(new ChunkedStream(byteArrayInputStream));
                byteArrayInputStream.close();

//        RandomAccessFile randomAccessFile = new RandomAccessFile("d:\\dev\\git\\sanshidi\\respo\\dev.flv", "r");
//        ctx.writeAndFlush(new ChunkedNioStream(randomAccessFile.getChannel()), ctx.newProgressivePromise());
//        ctx.writeAndFlush(new ChunkedStream(new FileInputStream(file)), ctx.newProgressivePromise());

                // write chunked stream:flv metadata
                ctx.channel().write(serverStream.getMeta());

                // write chunked stream:flv key frames
                for (RtmpMessage rtmpMessage : serverStream.getConfigMessages()) {
                    ctx.channel().write(rtmpMessage);
                }

                // add channel to serverStream's Subscribers
                serverStream.getSubscribers().add(ctx.channel());
            } else {
                sendError(ctx, NOT_ACCEPTABLE);
                return;
            }
        } else {
            sendError(ctx, NOT_ACCEPTABLE);
            return;
        }
//        ctx
//                .writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
//        ChannelFuture lastContentFuture = ctx
//                .writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
//        if (!isKeepAlive(request)) {
//            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
//        cause.printStackTrace();
        RtmpUtil.log(logger, ConstUtil.LogLevelEnum.Warn, "cause", cause.toString(), cause);
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    @Deprecated
    private String sanitizeUri(String uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }
        if (!uri.startsWith(url)) {
            return null;
        }
        if (!uri.startsWith("/")) {
            return null;
        }
        uri = uri.replace('/', File.separatorChar);
        if (uri.contains(File.separator + '.')
                || uri.contains('.' + File.separator) || uri.startsWith(".")
                || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
            return null;
        }
        return System.getProperty("user.dir") + File.separator + uri;
    }

    @Deprecated
    private static final Pattern ALLOWED_FILE_NAME = Pattern
            .compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    @Deprecated
    private static void sendListing(ChannelHandlerContext ctx, File dir) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append(" 目录：");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        buf.append("</ul></body></html>\r\n");
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer);
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
        response.headers().set(LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx,
                                  HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(CONTENT_TYPE,
                mimeTypesMap.getContentType(file.getPath()));
    }
}
