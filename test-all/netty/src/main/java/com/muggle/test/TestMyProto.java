package com.muggle.test;

import com.google.protobuf.InvalidProtocolBufferException;
import com.muggle.proto.TestMessage;

public class TestMyProto {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        TestMessage.Book build = TestMessage.Book.newBuilder().setAuthor("muggle").setName("java").setPage(10).build();
        byte[] bytes = build.toByteArray();
        TestMessage.Book book = TestMessage.Book.parseFrom(bytes);
        System.out.println(book);
    }
}
