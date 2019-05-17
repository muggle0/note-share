package com.muggle.proto;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

public class HttpClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;

    public HttpClientChannelInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
        }
        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpContentDecompressor());
        pipeline.addLast(new LoggingHandler());
        pipeline.addLast(new HttpClientMessageHandler());
    }
}
