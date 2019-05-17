package com.muggle.boot;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

/**
 * @program: poseidon-cloud
 * @description:
 * @author: muggle
 * @create: 2019-05-12
 **/

public class GrpcServer {
    private Server server;
    private void start() throws IOException {
        this.server= ServerBuilder.forPort(8899).addService(new MyService()).build().start();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
//           关机钩子
            System.out.println("关闭jvm");
            GrpcServer.this.stop();
        }));
        System.out.println("执行结束");
    }

    private void  stop(){
        if (null !=this.server){
            this.server.shutdown();
        }
    }

    private void awavitTerm() throws InterruptedException {
        if (null !=this.server){
            System.out.println(">>>>>>>>>>>>>>>>");
            this.server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        GrpcServer grpcServer = new GrpcServer();
        grpcServer.start();
        grpcServer.awavitTerm();
    }
}
