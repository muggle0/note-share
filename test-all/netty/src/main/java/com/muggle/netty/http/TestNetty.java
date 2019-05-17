package com.muggle.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author ：muggle
 * @date ：Created in 2019/3/11
 * @description：netty测试
 * @version:
 */
public class TestNetty {
    public static void main(String[] args) throws InterruptedException {
//        事件循环组 接收连接，将连接发送给work
        EventLoopGroup bossGroup = new NioEventLoopGroup();
//        work 干活
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
//      简化服务端启动
        ServerBootstrap serverBootstrap=new ServerBootstrap();
//                     打开通道                                                                                     子处理器 服务端初始化器
        serverBootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class).childHandler(new TestServerInitlizer());
        final ChannelFuture sync = serverBootstrap.bind(8081).sync();
        sync.channel().closeFuture().sync();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
