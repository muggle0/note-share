package com.muggle.netty.manySocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MyserverHandler extends SimpleChannelInboundHandler<String> {
//    处理方法
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        System.out.println("传过来的数据"+s);
        channelHandlerContext.channel().writeAndFlush("返回给客户端数据");
    }

}
