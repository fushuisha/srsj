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

import com.flazr.rtmp.RtmpConfig;
import com.flazr.rtmp.RtmpDecoder;
import com.flazr.rtmp.RtmpEncoder;
import com.flazr.util.StopMonitor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.PreferredDirectByteBufAllocator;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.naming.InitialContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RtmpServer implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RtmpServer.class);

    static {
        RtmpConfig.configureServer();
        CHANNELS = new DefaultChannelGroup("server-channels", new DefaultEventExecutor());
        APPLICATIONS = new ConcurrentHashMap<>();
        TIMER = new HashedWheelTimer(RtmpConfig.TIMER_TICK_SIZE, TimeUnit.MILLISECONDS);
    }

    public static final ChannelGroup CHANNELS;
    public static final Map<String, ServerApplication> APPLICATIONS;
    public static final Timer TIMER;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    public void run(int port) throws Exception {

//        final ChannelFactory factory = new ReflectiveChannelFactory(
//                Executors.newCachedThreadPool(),
//                Executors.newCachedThreadPool());

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .option(ChannelOption.ALLOCATOR, PreferredDirectByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(1024, 2048, 5096))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        /*
                         * (non-Javadoc)
                         *
                         * @see
                         * io.netty.channel.ChannelInitializer#initChannel(io
                         * .netty.channel.Channel)
                         */
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast("handshaker", new ServerHandshakeHandler());
                            ch.pipeline().addLast("decoder", new RtmpDecoder());
                            ch.pipeline().addLast("encoder", new RtmpEncoder());
//        pipeline.addLast("executor", new ExecutionHandler(
//                new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576)));
                            ch.pipeline().addLast("handler", new ServerHandler());
                        }
                    });
            ChannelFuture f = b.bind(port).sync();
            System.out.println("Start file server at port : " + port);
            f.channel().closeFuture().sync();


        } finally {
            // 优雅停机
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }

    public static void main(String[] args) throws Exception {
        int port = 1935;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        new RtmpServer().run(port);
    }

    private class ServerThread implements Runnable {
        @Override
        public void run() {
            int port = 1935;
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 100)
                        .option(ChannelOption.ALLOCATOR, PreferredDirectByteBufAllocator.DEFAULT)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(1024, 2048, 5096))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            /*
                             * (non-Javadoc)
                             *
                             * @see
                             * io.netty.channel.ChannelInitializer#initChannel(io
                             * .netty.channel.Channel)
                             */
                            public void initChannel(SocketChannel ch)
                                    throws Exception {
                                ch.pipeline().addLast("handshaker", new ServerHandshakeHandler());
                                ch.pipeline().addLast("decoder", new RtmpDecoder());
                                ch.pipeline().addLast("encoder", new RtmpEncoder());
                                ch.pipeline().addLast("handler", new ServerHandler());
                            }
                        });
                ChannelFuture f = b.bind(port).sync();
                System.out.println("Start file server at port : " + port);

                final Thread monitor = new StopMonitor(RtmpConfig.SERVER_STOP_PORT);
                monitor.start();

                f.channel().closeFuture().sync();

                TIMER.stop();
                final ChannelGroupFuture future = CHANNELS.close();
                logger.info("closing channels");
                future.awaitUninterruptibly();
//            logger.info("releasing resources");
//            factory.releaseExternalResources();
//            logger.info("server stopped");
            } catch (Exception ex) {
                logger.warn(ex.toString(), ex);
            } finally {
                // 优雅停机
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        taskExecutor.execute(new ServerThread());
    }
}
