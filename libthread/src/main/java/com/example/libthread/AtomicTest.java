package com.example.libthread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicTest {
    AtomicInteger count = new AtomicInteger(0);

    void m(){
        for (int i = 0; i < 10000; i++) {
            count.incrementAndGet();
            //System.out.println(Thread.currentThread().getName()+" i= "+i);
        }
    }

    public static void main(String[] args) {
        final AtomicTest atomicTest = new AtomicTest();

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    atomicTest.m();
                }
            }, "thread-" + i));
        }

        for (Thread t:threads) {
            t.start();
        }

        for (Thread t:threads) {
            try {
                t.join();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        System.out.println(atomicTest.count);
    }
}
