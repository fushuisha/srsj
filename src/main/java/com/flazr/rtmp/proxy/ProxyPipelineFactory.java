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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ReflectiveChannelFactory;

public class ProxyPipelineFactory implements ChannelFactory {

    private final ReflectiveChannelFactory cf;
    private final String remoteHost;
    private final int remotePort;

    public ProxyPipelineFactory(ReflectiveChannelFactory cf, String remoteHost, int remotePort) {
        this.cf = cf;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

//    @Override
//    public ChannelPipeline getPipeline() {
//        ChannelPipeline pipeline = Channels.pipeline();
//        pipeline.addLast("handshaker", new ProxyHandshakeHandler());
//        pipeline.addLast("handler", new ProxyHandler(cf, remoteHost, remotePort));
//        return pipeline;
//    }

    @Override
    public Channel newChannel() {
        Channel channel = cf.newChannel();
        channel.pipeline().addLast("handshaker", new ProxyHandshakeHandler())
                .addLast("handler", new ProxyHandler(cf, remoteHost, remotePort));
        return channel;
    }
}
