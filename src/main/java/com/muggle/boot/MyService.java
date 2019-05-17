package com.muggle.boot;

import com.muggle.proto.MyRequest;
import com.muggle.proto.MyResponse;
import com.muggle.proto.StudentProto;
import com.muggle.proto.StudentServiceGrpc;
import io.grpc.stub.StreamObserver;

/**
 * @program: poseidon-cloud
 * @description:
 * @author: muggle
 * @create: 2019-05-11
 **/


// grpc 实现机制
public class MyService extends StudentServiceGrpc.StudentServiceImplBase {

    @Override
    public void getRealName(MyRequest request, StreamObserver<MyResponse> responseObserver) {

        System.out.println("test>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        MyResponse test = MyResponse.newBuilder().setRealname("test").build();
        responseObserver.onNext(test);
        responseObserver.onCompleted();
    }
}
