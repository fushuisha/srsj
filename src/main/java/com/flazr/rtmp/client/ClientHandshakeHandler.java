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

package com.flazr.rtmp.client;

import com.flazr.rtmp.RtmpHandshake;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClientHandshakeHandler extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandshakeHandler.class);

    private boolean rtmpe;
    private final RtmpHandshake handshake;
    private boolean handshakeDone;

    public ClientHandshakeHandler(ClientOptions options) {
        handshake = new RtmpHandshake(options);
    }

//    @Override
//    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
//        logger.info("connected, starting handshake");
//        Channels.write(ctx, e.getFuture(), handshake.encodeClient0());
//        Channels.write(ctx, e.getFuture(), handshake.encodeClient1());
//    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write(handshake.encodeClient0());
        ctx.writeAndFlush(handshake.encodeClient1());
        super.channelActive(ctx);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < 1 + RtmpHandshake.HANDSHAKE_SIZE * 2) {
            return;
        }
        handshake.decodeServerAll(in);
//        Channels.write(ctx, Channels.succeededFuture(channel), handshake.encodeClient2());
        ctx.writeAndFlush(handshake.encodeClient2());
        handshakeDone = true;
        rtmpe = handshake.isRtmpe(); // rare chance server refused rtmpe
        if(handshake.getSwfvBytes() != null) {
            ClientHandler clientHandler = ctx.channel().pipeline().get(ClientHandler.class);
            clientHandler.setSwfvBytes(handshake.getSwfvBytes());
        }
        if(!rtmpe) {
            ctx.channel().pipeline().remove(this);
        }
//        Channels.fireChannelConnected(ctx, channel.getRemoteAddress());
//        return in;
    }

//    @Override
//    public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent ce) throws Exception {
//        if (!handshakeDone || !rtmpe || !(ce instanceof MessageEvent)) {
//            super.handleUpstream(ctx, ce);
//            return;
//        }
//        final MessageEvent me = (MessageEvent) ce;
//        if(me.getMessage() instanceof RtmpPublisher.Event) {
//            super.handleUpstream(ctx, ce);
//            return;
//        }
//        final ChannelBuffer in = (ChannelBuffer) ((MessageEvent) ce).getMessage();
//        handshake.cipherUpdateIn(in);
//        Channels.fireMessageReceived(ctx, in);
//    }
//
//    @Override
//    public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent ce) {
//        if (!handshakeDone || !rtmpe || !(ce instanceof MessageEvent)) {
//            ctx.sendDownstream(ce);
//            return;
//        }
//        final ChannelBuffer in = (ChannelBuffer) ((MessageEvent) ce).getMessage();
//        handshake.cipherUpdateOut(in);
//        ctx.sendDownstream(ce);
//    }

}
