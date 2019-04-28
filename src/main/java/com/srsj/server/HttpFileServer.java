/*
 * Copyright 2013-2018 Lilinfeng.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.srsj.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class HttpFileServer implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(HttpFileServer.class);

    private static final String DEFAULT_URL = "/sanshidi";

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    public void run(final int port, final String url) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast("http-decoder",
                                    new HttpRequestDecoder());
                            ch.pipeline().addLast("http-aggregator",
                                    new HttpObjectAggregator(65536));
                            ch.pipeline().addLast("http-encoder",
                                    new HttpResponseEncoder());
                            ch.pipeline().addLast("http-chunked",
                                    new ChunkedWriteHandler());
                            ch.pipeline().addLast("fileServerHandler",
                                    new HttpFileServerHandler(url));
                        }
                    });
            ChannelFuture future = b.bind(port).sync();
            System.out.println("HTTP文件目录服务器启动，网址是 : " + "http://127.0.0.1:8080/myapp/dev");
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        String url = DEFAULT_URL;
        if (args.length > 1)
            url = args[1];
        new HttpFileServer().run(port, url);
    }

    private class ServerThread implements Runnable {

        @Override
        public void run() {
            int port = 8080;
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch)
                                    throws Exception {
                                ch.pipeline().addLast("http-decoder",
                                        new HttpRequestDecoder());
                                ch.pipeline().addLast("http-aggregator",
                                        new HttpObjectAggregator(65536));
                                ch.pipeline().addLast("http-encoder",
                                        new HttpResponseEncoder());
                                ch.pipeline().addLast("http-chunked",
                                        new ChunkedWriteHandler());
                                ch.pipeline().addLast("fileServerHandler",
                                        new HttpFileServerHandler(DEFAULT_URL));
                                ch.pipeline().addLast("flvHandler",new FlvHandler());
                            }
                        });
                ChannelFuture future = b.bind(port).sync();
                System.out.println("HTTP文件目录服务器启动，网址是 : " + "http://127.0.0.1:8080/myapp/dev");
                future.channel().closeFuture().sync();
            } catch (Exception ex) {
                logger.warn(ex.toString(), ex);
            } finally {
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
