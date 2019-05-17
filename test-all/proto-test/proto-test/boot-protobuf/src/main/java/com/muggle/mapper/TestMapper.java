package com.muggle.mapper;


import com.muggle.entity.BookDTO;
import com.muggle.proto.TestMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring",uses=ObjectFactory.class)
public interface TestMapper {

    TestMessage.Book.Builder map(BookDTO bookDTO);
}
