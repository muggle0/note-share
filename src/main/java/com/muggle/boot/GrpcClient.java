package com.muggle.boot;

import com.muggle.proto.MyRequest;
import com.muggle.proto.MyResponse;
import com.muggle.proto.StudentServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * @program: poseidon-cloud
 * @description:
 * @author: muggle
 * @create: 2019-05-12
 **/

public class GrpcClient {
    public static void main(String[] args) {
        ManagedChannel managedChannel= ManagedChannelBuilder.forAddress("localhost",8899).usePlaintext(true).build();
        StudentServiceGrpc.StudentServiceBlockingStub studentServiceBlockingStub
                = StudentServiceGrpc.newBlockingStub(managedChannel);
        MyResponse response = studentServiceBlockingStub.getRealName(MyRequest.newBuilder().setUsername("ss").build());
        System.out.println(response.getRealname());
        /*
        * 服务器端和客户端建立的是一种TCP的连接
        * 连接会保存起来
        * 通过心跳检测机制来检测连接是否存活
        * */
    }
}
