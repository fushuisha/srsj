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

package com.flazr.rtmp.server;

import com.flazr.rtmp.RtmpHandshake;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class ServerHandshakeHandler extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(ServerHandshakeHandler.class);

    private final String zeroHexString = "00000000";
    private boolean handshakeDone = false;
    private boolean partOneDone = false;
    private final RtmpHandshake handshake= new RtmpHandshake();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        RtmpUtil.log(logger, "info", "in", in, null);
        if (!handshakeDone) {
            if(!partOneDone) {
                if(in.readableBytes() < RtmpHandshake.HANDSHAKE_SIZE + 1) {
                    return;
                }
                handshake.decodeClient0And1(in);
                ctx.write( handshake.encodeServer0());
                ctx.write( handshake.encodeServer1());
                ctx.write( handshake.encodeServer2());
                partOneDone = true;
                ctx.flush();
            }
            if(!handshakeDone) {
                if(in.readableBytes() < RtmpHandshake.HANDSHAKE_SIZE) {
                    return;
                }
                handshake.decodeClient2(in);
                handshakeDone = true;
                logger.info("handshake done, rtmpe: {}", handshake.isRtmpe());
                if(Arrays.equals(handshake.getPeerVersion(), Hex.decodeHex(zeroHexString))) {
                    // TODO
                    final ServerHandler serverHandler = ctx.pipeline().get(ServerHandler.class);
                    serverHandler.setAggregateModeEnabled(false);
                    logger.info("old client version, disabled 'aggregate' mode");
                }
                if(!handshake.isRtmpe()) {
                    ctx.channel().pipeline().remove(this);
                }
            }
        }

    }
}
