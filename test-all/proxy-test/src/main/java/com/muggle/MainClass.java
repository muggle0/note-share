package com.muggle;

import java.lang.reflect.Proxy;

public class MainClass {
    public static void main(String[] args) {

//        Proxy 代理器  invocationHandler 增强器

        // 创建一个代理对象
        MyInvocationHandler myInvocationHandler = new MyInvocationHandler(new MySubjectImpl());
        MySubject mySubject = (MySubject) Proxy.newProxyInstance(MySubjectImpl.class.getClassLoader(), MySubjectImpl.class.getInterfaces(), myInvocationHandler);
        // 调用代理的方法
        System.out.println(mySubject.test());
    }
}
