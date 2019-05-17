package com.muggle.proto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;

import java.util.logging.Logger;

public class HttpClientMessageHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static Logger log = Logger.getLogger(HttpClientMessageHandler.class.getName());


    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject httpObject) throws Exception {
        if (httpObject instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) httpObject;
            System.out.println("CONTENT_TYPE:" + response.headers().get(HttpHeaders.Names.CONTENT_TYPE));
//
        }
        if(httpObject instanceof HttpContent)
        {
            HttpContent content = (HttpContent)httpObject;
            ByteBuf buf = content.content();
            byte[] array =null;
            if(buf.hasArray()) {
                array=buf.array();
            }else {
                array = new byte[buf.readableBytes()];
                buf.getBytes(buf.readerIndex(), array);
            }

            try {
                TestMessage.Book book = TestMessage.Book.parseFrom(array);
                System.out.println(book.toString());
            }catch (Exception e){

            }
//            System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));


            buf.release();
        }
        System.out.println("test>>>>");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("1             ：通道处于活动状态");
        super.channelActive(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("2             ：通道注册");
        super.channelRegistered(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("1             ：处理器添加");

        super.handlerAdded(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("5             ：通道数据进入");

        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("6             ：通道取消注册");
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.severe("通道异常》》》》 "+cause.getMessage());
        ctx.close();
    }
}
