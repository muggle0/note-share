package com.muggle.rpc.client;

import com.muggle.proto.TestMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class MyClient {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(boss).channel(NioSocketChannel.class).handler(new ClientInitializer());
            ChannelFuture localhost = bootstrap.connect("127.0.0.1", 8081).sync();
            Channel channel = localhost.channel();
            channel.writeAndFlush(TestMessage.Book.newBuilder().setPage(10).setAuthor("ss").setName("ss").build());
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
        }
    }
}
