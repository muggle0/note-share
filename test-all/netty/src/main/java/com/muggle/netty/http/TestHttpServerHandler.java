package com.muggle.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;


/**
 * @author ：muggle
 * @date ：Created in 2019/3/11
 * @description：http处理器x
 * @version:
 */
public class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
//    构造一个Http响应
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject httpObject) throws Exception {
        System.out.println("请求读取处理4");
        ByteBuf buffer=Unpooled.copiedBuffer("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>这是一个Netty构造的http响应</h1>\n" +
                "</body>\n" +
                "</html>", CharsetUtil.UTF_8);
        FullHttpResponse response=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,buffer);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,buffer.readableBytes());
        channelHandlerContext.writeAndFlush(response);
       channelHandlerContext.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("通道处于活动状态 3");
        super.channelActive(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("通道注册 2");
        super.channelRegistered(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("处理器添加 1");
        super.handlerAdded(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("通道数据进入5");
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("通道取消注册6");
        super.channelUnregistered(ctx);
    }
}
