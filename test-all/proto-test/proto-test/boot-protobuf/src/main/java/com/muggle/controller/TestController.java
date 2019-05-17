package com.muggle.controller;


import com.muggle.entity.BookDTO;
import com.muggle.mapper.TestMapper;
import com.muggle.proto.TestMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/a")
public class TestController {
    @Autowired
    TestMapper mapper;
    @GetMapping(value = "/test",produces = "application/x-protobuf")
    public TestMessage.Book test(){
        System.out.println("test");
        return TestMessage.Book.newBuilder().setAuthor("ss").setPage(1).build();
    }

    @PostMapping(value = "/test1",produces = "application/x-protobuf")
    public TestMessage.Book test1(@RequestBody TestMessage.Book book){
        System.out.println("test");
        return TestMessage.Book.newBuilder().setPage(10).setAuthor("ss").build();
    }

    @GetMapping(value = "/test2",produces = "application/x-protobuf")
    public TestMessage.Book test2(String author){
        System.out.println(author);
        return TestMessage.Book.newBuilder().setAuthor("ss").setPage(1).build();
    }

    @GetMapping(value = "/test3",produces = "application/x-protobuf")
    public TestMessage.Book test3(){
        BookDTO bookDTO = new BookDTO();
        bookDTO.setAuthor("ss");
        bookDTO.setPage(1);
        TestMessage.Book.Builder map = mapper.map(bookDTO);
        System.out.println(map.getAuthor());
        return map.build();
    }
}
