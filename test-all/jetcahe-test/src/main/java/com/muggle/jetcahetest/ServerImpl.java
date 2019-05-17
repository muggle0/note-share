package com.muggle.jetcahetest;

import org.springframework.stereotype.Service;

/**
 * @program: poseidon-cloud
 * @description:
 * @author: muggle
 * @create: 2019-05-17
 **/
@Service
public class ServerImpl implements Server {
    @Override
    public int test(String message) {
        System.out.println(">>>");
        return 666;
    }
}
