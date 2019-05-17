package com.muggle;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;

public class NioTest {
    public static void main(String[] args) throws IOException {
////        需要通过InputStream，OutputStream或RandomAccessFile获取FileChannel
//        File file = new File("D:\\workspace\\java\\nio-test\\src\\main\\resources\\test.txt");
//        File file2 = new File("D:\\workspace\\java\\nio-test\\src\\main\\resources\\test2.txt");
//        FileInputStream fin = new FileInputStream(file);
//        FileInputStream fin2 = new FileInputStream(file2);
//
//        FileChannel channel = fin.getChannel();
//        FileChannel channel2 = fin2.getChannel();
////        channel2.transferFrom(channel,0,channel.size());
//        ByteBuffer buf = ByteBuffer.allocate(10);
//        int read = channel.read(buf);
//        while (read>0){
//            buf.flip();
//            byte[] array = new byte[10];
////            产生数据残留的原因：clear的方法重置游标，toarray 会把无效数据读入
//            int limit = buf.limit();
//            if (limit>=10){
//
//                buf.get(array);
//            }else {
//                array=new byte[limit];
//                buf.get(array);
//            }
//            String s = new String(array);
//            System.out.print(s);
//            buf.clear();
//            read = channel.read(buf);
//        }
//        buf.w
//        channel.write()
//        fin.close();

       /* byte[] bytes = new byte[4];
//        解释，utf-8包括ASCII码表，抽出标识位标识汉字或者其他
        bytes[0]=-27;
        bytes[1]=-91;
        bytes[2]=-67;
        bytes[3]=-94;
        String s = new String(bytes,"utf-8");
        System.out.println(s);
        byte[] bytes = new byte[4];

        while (fin.read(bytes) > 0) {
            String str = new String(bytes);
            System.out.print(str);
        }
        */
       /* RandomAccessFile rw = new RandomAccessFile("D:\\workspace\\java\\nio-test\\src\\main\\resources\\test.txt", "rw");
        FileChannel channel = rw.getChannel();

        ByteBuffer allocate = ByteBuffer.allocate(1000);
        allocate.put("wwwww".getBytes());
        allocate.flip();
        channel.write(allocate);

*//*


        channel.write(wrap);*//*
//      wrap 不需要 flip()
        ByteBuffer wrap = ByteBuffer.wrap("sssssssssssss".getBytes());
//        wrap.flip();
        File file2 = new File("D:\\workspace\\java\\nio-test\\src\\main\\resources\\test2.txt");
        FileOutputStream fout=new FileOutputStream(file2);
        FileChannel channel1 = fout.getChannel();
        channel1.write(wrap);
        rw.close();*/

        RandomAccessFile rw = new RandomAccessFile("D:\\workspace\\java\\nio-test\\src\\main\\resources\\test.txt", "rw");
        RandomAccessFile rw2 = new RandomAccessFile("D:\\workspace\\java\\nio-test\\src\\main\\resources\\test2.txt", "rw");
        FileChannel channel = rw.getChannel();
        FileChannel channel2 = rw2.getChannel();
        channel2.transferFrom(channel,0,channel.size());
    }
}
