package com.muggle.zerocopy;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class CopyTest {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8081);
        while (true){
            Socket accept = serverSocket.accept();
            InputStream inputStream = accept.getInputStream();
            byte[] bytes=new byte[1024];
            inputStream.read(bytes);
        }

    }
}
