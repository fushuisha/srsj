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

import com.flazr.rtmp.RtmpEncoder;
import com.flazr.rtmp.RtmpMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

//@ChannelPipelineCoverage("one")
public class ProxyEncoder extends SimpleChannelInboundHandler<RtmpMessage> {

    private final RtmpEncoder encoder = new RtmpEncoder();

//    @Override
//    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
//        Channels.fireMessageReceived(ctx, encoder.encode((RtmpMessage) e.getMessage()));
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RtmpMessage msg) throws Exception {
//        Channels.fireMessageReceived(ctx, encoder.encode((RtmpMessage) e.getMessage()));
        ctx.fireChannelRead(encoder.encode(msg));
    }
}
