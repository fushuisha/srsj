/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.flazr.rtmp.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

//@ChannelPipelineCoverage("one")
public class ProxyHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(ProxyHandler.class);

    private final ReflectiveChannelFactory cf;
    private final String remoteHost;
    private final int remotePort;

    private volatile Channel outboundChannel;

    public ProxyHandler(ReflectiveChannelFactory cf, String remoteHost, int remotePort) {
        this.cf = cf;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

//    @Override
//    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
//        final Channel inboundChannel = e.getChannel();
//        RtmpProxy.ALL_CHANNELS.add(inboundChannel);
//        inboundChannel.setReadable(false);
//        ClientBootstrap cb = new ClientBootstrap(cf);
//        cb.getPipeline().addLast("handshaker", new ProxyHandshakeHandler());
//        cb.getPipeline().addLast("handler", new OutboundHandler(e.getChannel()));
//        ChannelFuture f = cb.connect(new InetSocketAddress(remoteHost, remotePort));
//        outboundChannel = f.getChannel();
//        f.addListener(new ChannelFutureListener() {
//            @Override public void operationComplete(ChannelFuture future) throws Exception {
//                if (future.isSuccess()) {
//                    logger.info("connected to remote host: {}, port: {}", remoteHost, remotePort);
//                    inboundChannel.setReadable(true);
//                } else {
//                    inboundChannel.close();
//                }
//            }
//        });
//    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        logger.info("channel opened: {}", ctx.channel());
//        super.channelActive(ctx);

        final Channel inboundChannel = ctx.channel();
        RtmpProxy.ALL_CHANNELS.add(inboundChannel);
//        inboundChannel.setReadable(false);
        Bootstrap cb = new Bootstrap();
        cb.channelFactory(cf);
        cb.handler(new ChannelInitializer<SocketChannel>() {
            /*
             * (non-Javadoc)
             *
             * @see
             * io.netty.channel.ChannelInitializer#initChannel(io
             * .netty.channel.Channel)
             */
            public void initChannel(SocketChannel ch)
                    throws Exception {
                ch.pipeline().addLast("handshaker", new ProxyHandshakeHandler());
                ch.pipeline().addLast("handler", new OutboundHandler(ctx.channel()));
            }
        });
//        cb.getPipeline().addLast("handshaker", new ProxyHandshakeHandler());
//        cb.getPipeline().addLast("handler", new OutboundHandler(e.getChannel()));
        ChannelFuture f = cb.connect(new InetSocketAddress(remoteHost, remotePort));
        outboundChannel = f.channel();
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("connected to remote host: {}, port: {}", remoteHost, remotePort);
//                    inboundChannel.setReadable(true);
                } else {
                    inboundChannel.close();
                }
            }
        });
    }

//    @Override
//    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
//        ChannelBuffer in = (ChannelBuffer) e.getMessage();
//        // logger.debug(">>> [{}] {}", in.readableBytes(), ChannelBuffers.hexDump(in));
//        outboundChannel.write(in);
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
//        ChannelBuffer in = (ChannelBuffer) e.getMessage();
        // logger.debug(">>> [{}] {}", in.readableBytes(), ChannelBuffers.hexDump(in));
        outboundChannel.write(msg);
    }

//    @Override
//    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
//        logger.info("closing inbound channel");
//        if (outboundChannel != null) {
//            closeOnFlush(outboundChannel);
//        }
//    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("closing inbound channel");
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
//        logger.info("inbound exception: {}", e.getCause().getMessage());
//        closeOnFlush(e.getChannel());
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.info("inbound exception: {}", cause.getMessage());
        closeOnFlush(ctx.channel());
    }

    //    @ChannelPipelineCoverage("one")
    private class OutboundHandler extends SimpleChannelInboundHandler {

        private final Channel inboundChannel;

        public OutboundHandler(Channel inboundChannel) {
            logger.info("opening outbound channel");
            this.inboundChannel = inboundChannel;
        }

//        @Override
//        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
//            ChannelBuffer in = (ChannelBuffer) e.getMessage();
//            // logger.debug("<<< [{}] {}", in.readableBytes(), ChannelBuffers.hexDump(in));
//            inboundChannel.write(in);
//        }

//        @Override
//        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
//            logger.info("closing outbound channel");
//            closeOnFlush(inboundChannel);
//        }
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            logger.info("closing outbound channel");
            closeOnFlush(inboundChannel);
        }

//        @Override
//        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
//            logger.info("outbound exception: {}", e.getCause().getMessage());
//            closeOnFlush(e.getChannel());
//        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                throws Exception {
            logger.info("outbound exception: {}", cause.getMessage());
            closeOnFlush(ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf) msg;
            // logger.debug("<<< [{}] {}", in.readableBytes(), ChannelBuffers.hexDump(in));
            inboundChannel.write(in);
        }
    }

    static void closeOnFlush(Channel ch) {
        if (ch.isOpen()) {
            ch.write(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

}
