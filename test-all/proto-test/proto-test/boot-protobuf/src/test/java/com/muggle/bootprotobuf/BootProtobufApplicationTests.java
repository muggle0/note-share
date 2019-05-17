package com.muggle.bootprotobuf;

import com.google.protobuf.Descriptors;
import com.muggle.entity.BookDTO;
import com.muggle.mapper.TestMapper;
import com.muggle.proto.TestMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import  org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class BootProtobufApplicationTests {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();   //构造MockMvc
    }

    @Test
    public void contextLoads() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/a/test").contentType("application/x-protobuf")).andReturn().getResponse();
        System.out.println("test");
    }

    @Test
    public void test2() throws Exception {
        byte[] sses = TestMessage.Book.newBuilder().setAuthor("ss").setPage(10).build().toByteArray();
        MockHttpServletResponse response = mockMvc.perform(
                post("/a/test1").contentType("application/x-protobuf")
                        .content(sses)).andReturn().getResponse();
        System.out.println("test");
    }

    @Test
    public void testLocalLogin() throws Exception{
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/a/test2")
                .param("author", "ss")
                .param("page", "11"));
        MvcResult mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()).andReturn();
        TestMessage.Book book = TestMessage.Book.parseFrom(mvcResult.getResponse()
                .getContentAsByteArray());
        System.out.println(book.getAuthor());
    }

    @Test
    public void test3() throws Exception {
        byte[] sses = TestMessage.Book.newBuilder().setAuthor("ss").setPage(10).build().toByteArray();
        MockHttpServletResponse response = mockMvc.perform(
                get("/a/test2").contentType("application/x-protobuf")
                        .content(sses)).andReturn().getResponse();
        System.out.println("test");
    }





    /*public static void setMessageBuilder(com.google.protobuf.GeneratedMessage.Builder message, Descriptors.Descriptor descriptor, Object srcObject) throws Exception {
        String cname = srcObject.getClass().getName();
        *//*BeanMapper.getSimpleProperties -- this is a warpper method that gets the list of property names*//*
        List<String> simpleProps = BeanMapper.getSimpleProperties(srcObject.getClass());

        Map map = new HashMap();
        for (String pName : simpleProps) {
            System.out.println(" processing property "+ pName);
            Object value= PropertyUtils.getProperty(srcObject, pName);
            if(value==null) continue;

            Descriptors.FieldDescriptor fd=descriptor.findFieldByName(pName) ;

            System.out.println(" property "+  pName+" , found fd :"+ (fd==null ? "nul":"ok"));
            message.setField(fd, value);
            System.out.println(" property "+  pName+"  set ok,");

        }
        return ;
    }*/

    @Test
    public void test4() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                get("/a/test3").contentType("application/x-protobuf"))
                .andReturn().getResponse();
        System.out.println("test");
    }

}
