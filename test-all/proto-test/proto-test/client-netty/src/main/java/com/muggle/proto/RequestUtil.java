package com.muggle.proto;

import io.netty.handler.codec.http.*;

import java.net.URI;

public class RequestUtil {
    public static HttpRequest getRequest( RequestObj requestObj){
        HttpRequest request = new DefaultFullHttpRequest(requestObj.getHttpVersion(), requestObj.getMethod(), requestObj.getUrl());
        request.headers().set(HttpHeaders.Names.HOST, requestObj.getHost());
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
        request.headers().set(HttpHeaders.Names.COOKIE,ClientCookieEncoder.encode(
                new DefaultCookie("my-cookie", "foo"),
                new DefaultCookie("another-cookie", "bar")));
        return request;
    }
}
