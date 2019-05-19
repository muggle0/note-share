package com.muggle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MyInvocationHandler implements InvocationHandler {

    private MySubject subject;
    int i=0;

    public MyInvocationHandler(MySubject subject) {
        this.subject = subject;
    }

    //    http://stackoverflow.com/questions/22930195/understanding-proxy-arguments-of-the-invoke-method-of-java-lang-reflect-invoca
    public Object invoke(Object subjectImpl, Method method, Object[] args) throws Throwable {
        System.out.println("开始执行动态代理");
        i++;
        Object invoke = method.invoke(subject, args);
       if (i>10){
           return "hahahah";
       }
        String test = ((MySubject) subjectImpl).test();
//        method.invoke(args);
        return  test;
//    subjectImpl    1. 可以使用反射获取代理对象的信息（也就是proxy.getClass().getName()）。
//
//2. 可以将代理对象返回以进行连续调用，这就是proxy存在的目的，因为this并不是代理对象。
    }

}
