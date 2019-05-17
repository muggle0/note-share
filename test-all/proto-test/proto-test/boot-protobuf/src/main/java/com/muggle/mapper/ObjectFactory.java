package com.muggle.mapper;

import com.muggle.proto.TestMessage;
import org.springframework.stereotype.Service;

@Service
public class ObjectFactory {
    public TestMessage.Book.Builder test(){
        return TestMessage.Book.newBuilder();
    }
}
