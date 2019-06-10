package com.muggle.test;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.anno.CreateCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestApplicationTests {
    @Autowired
    TestAsync testAsync;

    @Autowired
    Validator validator;
    @CreateCache(expire = 100)
    private Cache<Long, VolidatorBean> cache;
    @Autowired
    Myservice myservice;
    @Test
    public void contextLoads() {
        long l = System.currentTimeMillis();
        for (int i=0;i<10000;i++){
           IcGeneralBody test = new IcGeneralBody().setBarcode("test").setAccount("11.11").setActuallyCost("11.11").setBtmFee("11.12")
                   .setBuyNumPrice(1211.0).setCetPre(121.11).setColorId(11l);
           VolidatorBean volidatorBean = new VolidatorBean();
           volidatorBean.setId(1L);
//        myservice.insertVolidatorBean(volidatorBean);
           Set<ConstraintViolation<IcGeneralBody>> validate = validator.validate(test);
           validate.forEach(csss->{

               System.out.println(csss.getMessage());
           });
           System.out.println(",,,,");
       }
        long l1 = System.currentTimeMillis();
        System.out.println(l1-l);
//        validator.validate()
    }
    @Test
    public void test() throws InterruptedException {
        long l = System.currentTimeMillis();
        Future<String> task1 =  testAsync.doTest();
        Future<String> task2 =  testAsync.otherThing();
        Future<String> task3 =  testAsync.someThing();
        while (true){
            if (task1.isDone()&&task2.isDone()&&task3.isDone()){
                System.out.println("<><><><><><><><><><><>");
                long l1 = System.currentTimeMillis();
                long l2=l1-l;
                System.out.println( "aaa:" + l2);
                break;
            }
        }

        Thread.sleep(10000);
    }

    @Test
    public void test2() throws InterruptedException {
        long l = System.currentTimeMillis();
        Future<String> task1 =  testAsync.doTest();
        Future<String> task2 =  testAsync.otherThing();
        Future<String> task3 =  testAsync.someThing();
        System.out.println("<><><<<<<<<<<<<<><><><>>>>>>>>>>>>>><><>");
        long l1 = System.currentTimeMillis();
        long l2=l1-l;
        System.out.println( "aaa:" + l2);
        Thread.sleep(10000);
    }

    @Test
    public void test3() throws InterruptedException {
        long l = System.currentTimeMillis();
        Thread.sleep(100);
        System.out.println("你好");
        Thread.sleep(200);
        System.out.println("我好");
        Thread.sleep(200);
        System.out.println("大家好");

        System.out.println("<><><<<<<<<<<<<<><><><>>>>>>>>>>>>>><><>");
        long l1 = System.currentTimeMillis();
        long l2=l1-l;
        System.out.println( "aaa:" + l2);
        Thread.sleep(10000);
    }
    @Test
    public void testDate(){
        Date date=new Date();
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyydd");
        System.out.println(dateFormat.format(date));
    }
}

