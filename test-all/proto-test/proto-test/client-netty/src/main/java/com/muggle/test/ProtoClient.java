//package com.muggle.test;
//
//import com.muggle.proto.TestMessage;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.nio.NioSocketChannel;
//
//public class ProtoClient {
//    public static void main(String[] args) {
//        NioEventLoopGroup group = new NioEventLoopGroup();
//        try {
//            Bootstrap bootstrap = new Bootstrap();
//            bootstrap.group(group).channel(NioSocketChannel.class).handler(new ProtoChannelInitializer());
//
//            ChannelFuture sync = bootstrap.connect("127.0.0.1", 8080).sync();
//            Channel channel = sync.channel();
//            channel.writeAndFlush(TestMessage.Book.newBuilder().setPage(10).setAuthor("ss").build());
//            channel.closeFuture().sync();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//
//        }
//    }
//}
