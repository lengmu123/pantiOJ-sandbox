package com.xy.unsafe;

import java.util.ArrayList;
import java.util.List;

/**
 * 无限占用内存
 */
public class MemoryError {

    public static void main(String[] args) {
        List<byte[]> bytes = new ArrayList<>();
       while (true){
           bytes.add(new byte[1024*1024*10]);
       }
    }
}