package com.muggle.locktest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LockTestApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void test1(){

        BigDecimal bigDecimal = new BigDecimal("20.1");
        BigDecimal bigDecimal1 = new BigDecimal("-20.2");
        BigDecimal add = bigDecimal.add(bigDecimal1);
        System.out.println(add);
        List<String> test=new ArrayList<>();
        test.add("a");
        test.add("b");
        test.add("c");
        test.add("d");
        test.add("e");
        int ss=test.size()/2;
        List<String>  [] test2=new List[ss+1];
        for (int i=0;i<ss;i++){
            test2[i]=test.subList(i*2,(i+1)*2);
        }
        test2[ss]=test.subList(ss*2,test.size());
        List<String> list = test.subList(0, 2);
        list.clear();
        System.out.println(test.get(0));

    }

    @Test
    public void  test2(){
        int a=1,c=10;
        do {
            if (a>c){
                break;
            }
            c--;
        }while (++a<5);
        System.out.println(a);
        System.out.println(c);
        int [] aa=new int[10];
        TestLock testLock = new TestLock();
        Runnable getId = testLock::getId;
    }



}
