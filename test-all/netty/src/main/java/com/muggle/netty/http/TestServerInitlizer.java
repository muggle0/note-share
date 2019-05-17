package com.muggle.netty.http;

import com.muggle.netty.http.TestHttpServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @author ：muggle
 * @date ：Created in 2019/3/11
 * @description：
 * @version:
 */
// 初始化器 channel注册好之后 自动创建 执行代码
public class TestServerInitlizer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        final ChannelPipeline pipeline = socketChannel.pipeline();
//        对web响应编解码
        pipeline.addLast("httpserverCodec",new HttpServerCodec());
//        起名加入管道  自己的处理器
        pipeline.addLast("testHttpResponse",new TestHttpServerHandler());
    }
}
