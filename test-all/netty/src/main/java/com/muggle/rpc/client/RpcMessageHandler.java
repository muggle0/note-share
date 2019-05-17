package com.muggle.rpc.client;

import com.muggle.proto.TestMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcMessageHandler extends SimpleChannelInboundHandler<TestMessage.Book> {
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TestMessage.Book book) throws Exception {
        System.out.println("test");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
    }
}
