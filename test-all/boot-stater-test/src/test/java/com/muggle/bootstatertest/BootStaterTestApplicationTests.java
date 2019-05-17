package com.muggle.bootstatertest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BootStaterTestApplicationTests {

    @Test
    public void contextLoads() {
        test test = new test();
        Class<? extends com.muggle.bootstatertest.test> aClass = test.getClass();
        Class<Muggle> muggleClass = Muggle.class;
        boolean annotationPresent = aClass.isAnnotationPresent(muggleClass);
        System.out.println(annotationPresent);
        Annotation[] annotations = aClass.getAnnotations();
        Muggle annotation = aClass.getAnnotation(Muggle.class);
        Field[] fields = aClass.getFields();
        Annotation[] annotations1 = fields[0].getAnnotations();
        Method[] methods = aClass.getMethods();
        Method[] declaredMethod = aClass.getDeclaredMethods();
        Annotation[] declaredAnnotations = methods[0].getDeclaredAnnotations();

    }

}

@Target(ElementType.TYPE )
@interface Muggle{
    String test() default "ss";
}
@Muggle(test = "ss")
class test{}