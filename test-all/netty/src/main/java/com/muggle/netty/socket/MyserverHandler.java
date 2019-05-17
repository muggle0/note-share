package com.muggle.netty.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class MyserverHandler extends SimpleChannelInboundHandler<String> {
//    存储通道
    private static ChannelGroup group=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
//    处理方法
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        Channel channel = ctx.channel();
        group.writeAndFlush(ctx.channel().remoteAddress()+">>>>"+s);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();

        ctx.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        group.writeAndFlush(channel.remoteAddress()+"连接地址》》》》》》》");
        group.add(channel);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        group.writeAndFlush(channel.remoteAddress()+"断开连接》》》》》》》");
    }
}
