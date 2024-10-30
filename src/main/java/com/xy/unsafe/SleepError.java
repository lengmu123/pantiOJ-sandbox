package com.xy.unsafe;

/**
 * 无限睡眠异常
 */
public class SleepError {
    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
        System.out.println("睡完了");
    }
}
