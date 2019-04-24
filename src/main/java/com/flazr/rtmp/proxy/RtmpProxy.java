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

import com.flazr.rtmp.RtmpConfig;
import com.flazr.util.StopMonitor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class RtmpProxy {

    private static final Logger logger = LoggerFactory.getLogger(RtmpProxy.class);

    static {
        RtmpConfig.configureProxy();
        ALL_CHANNELS = new DefaultChannelGroup("rtmp-proxy", new DefaultEventExecutor());
    }

    protected static final ChannelGroup ALL_CHANNELS;

    public static void main(String[] args) throws Exception {

//        Executor executor = Executors.newCachedThreadPool();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ReflectiveChannelFactory cf = new ReflectiveChannelFactory<>(NioSocketChannel.class);
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channelFactory(new ProxyPipelineFactory(cf,
                    RtmpConfig.PROXY_REMOTE_HOST, RtmpConfig.PROXY_REMOTE_PORT));
            InetSocketAddress socketAddress = new InetSocketAddress(RtmpConfig.PROXY_PORT);
            ChannelFuture f = b.bind(socketAddress).sync();
            f.channel().closeFuture().sync();
            logger.info("proxy server started, listening on {}", socketAddress);

            Thread monitor = new StopMonitor(RtmpConfig.PROXY_STOP_PORT);
            monitor.start();
            monitor.join();

            ChannelGroupFuture future = ALL_CHANNELS.close();
            logger.info("closing channels");
            future.awaitUninterruptibly();
            logger.info("releasing resources");
        } finally {
            // 优雅停机
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("server stopped");
        }
    }

}
