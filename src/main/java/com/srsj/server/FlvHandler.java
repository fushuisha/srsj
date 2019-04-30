package com.srsj.server;

import com.flazr.io.flv.FlvHttpWriter;
import com.flazr.rtmp.RtmpMessage;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlvHandler extends ChannelDuplexHandler {
    private final Logger logger = LoggerFactory.getLogger(FlvHandler.class);

    private FlvHttpWriter recorder = new FlvHttpWriter();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof RtmpMessage) {
            recorder.setOut(ctx.channel());
            recorder.write((RtmpMessage) msg);
        } else {
            super.write(ctx, msg, promise);
        }
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        super.close(ctx, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        super.exceptionCaught(ctx, cause);
    }
}
