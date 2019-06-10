package com.muggle.test;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * @program: test
 * @description: 测试
 * @author: muggle
 * @create: 2019-01-07 10:04
 **/
@Component
public class TestAsync {
    @Async
    public Future<String> doTest() throws InterruptedException {
        Thread.sleep(100);
        System.out.println("你好");
        return new AsyncResult<>("任务一完成");
    }
    @Async
    public Future<String> someThing() throws InterruptedException {
        Thread.sleep(200);
        System.out.println("我好");
        return new AsyncResult<>("任务222222完成");
    }

    @Async
    public Future<String> otherThing() throws InterruptedException {
        Thread.sleep(200);
        System.out.println("大家好");
        Double.parseDouble("9" + "222");
        return new AsyncResult<>("任务33333完成");
    }
}
