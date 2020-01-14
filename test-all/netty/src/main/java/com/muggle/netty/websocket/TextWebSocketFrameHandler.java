package com.muggle.netty.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @program: netty
 * @description:
 * @author: muggle
 * @create: 2020-01-14
 **/

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String text = textWebSocketFrame.text();
        System.out.println(text+">>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Channel channel = channelHandlerContext.channel();
        channel.writeAndFlush(new TextWebSocketFrame("99999"));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        super.handlerAdded(ctx);
    }
}
