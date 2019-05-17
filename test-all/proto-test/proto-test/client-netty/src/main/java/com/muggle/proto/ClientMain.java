package com.muggle.proto;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.net.URI;
import java.util.logging.Logger;

public class ClientMain {
    static final String URL = System.getProperty("url", UriProperties.URL);
    private static Logger log = Logger.getLogger(ClientMain.class.getName());
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        log.info("author：muggle  isocket@outlook.com");
        String path = UriProperties.PATH;
        String scheme = UriProperties.HTTP;
        String host = UriProperties.HOST;
        Integer port = UriProperties.PORT;
        if (port==null){
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            log.severe("请求不是http 和https");
            return;
        }
        final boolean ssl = "https".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
            log.info("工具发起一个 https 请求》》》》》》");
        }else {
            log.info("工具发起一个 http 请求》》》》》》");
            sslCtx = null;
        }

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).handler(new HttpClientChannelInitializer(sslCtx));
            Channel ch = bootstrap.connect(host, port).sync().channel();
            log.info("构造 request 》》》》》》》");

            log.info("demo >>>>>>>");
            RequestObj requestObj = new RequestObj();
            requestObj.setUrl(path+"?a=ss&b=22");
            requestObj.setHttpVersion(HttpVersion.HTTP_1_1);
            requestObj.setMethod(HttpMethod.GET);
            requestObj.setHost(host);

            HttpRequest request = RequestUtil.getRequest( requestObj);
            ch.writeAndFlush(request);
            ch.closeFuture().sync();
        }finally {
            log.info("end >>>>>>>>>>>>>");
//            优雅关机
            group.shutdownGracefully();
        }
    }
}
