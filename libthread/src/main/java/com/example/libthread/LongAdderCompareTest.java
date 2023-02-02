package com.example.libthread;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class LongAdderCompareTest {
    static long count2 = 0;
    static AtomicLong count1 = new AtomicLong(0);
    static LongAdder count3 = new LongAdder();

    public static void main(String[] args) throws Exception{
        Thread[] threads = new Thread[1000];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(()->{
                for (int j = 0; j < 100000; j++) {
                    count1.incrementAndGet();
                }
            });
        }

        long start = System.currentTimeMillis();

        for (Thread t: threads) {
            t.start();
        }

        for (Thread t: threads) {
            t.join();
        }

        long end = System.currentTimeMillis();

        System.out.println("Atomic: "+count1.get()+"  time "+(end - start));

        //-----------------------------------------
        Object lock = new Object();

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(()->{
                for (int j = 0; j < 100000; j++) {
                    synchronized (lock){
                        count2++;
                    }
                }
            });
        }

        start = System.currentTimeMillis();

        for (Thread t: threads) {
            t.start();
        }

        for (Thread t: threads) {
            t.join();
        }

        end = System.currentTimeMillis();

        System.out.println("Sync: "+count2+"  time "+(end - start));

        //-----------------------------------------

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(()->{
                for (int j = 0; j < 100000; j++) {
                    count3.increment();
                }
            });
        }

        start = System.currentTimeMillis();

        for (Thread t: threads) {
            t.start();
        }

        for (Thread t: threads) {
            t.join();
        }

        end = System.currentTimeMillis();

        System.out.println("LongAdder: "+count3.longValue()+"  time "+(end - start));
    }

    /**
     * 打印输出 ：
     * Atomic: 100000000  time 1772
     * Sync: 100000000  time 1357
     * LongAdder: 100000000  time 282
     */
}
