package com.example.libthread;

import java.util.concurrent.TimeUnit;

public class ThreadYieldAndJoin {
    public static void main(String[] args) {
        ThreadYieldAndJoin threadYieldAndJoin = new ThreadYieldAndJoin();
        //threadYieldAndJoin.testYield();
        threadYieldAndJoin.testJoin();
    }

    private void testYield(){
        new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        System.out.println("A"+i);
                        if(i%10 == 0){
                            Thread.yield();
                        }
                    }
                }
            }
        ).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    System.out.println("-----B"+i);
                    if(i%10 == 0){
                        Thread.yield();
                    }
                }
            }
        }
        ).start();
    }


    private void testJoin(){
        final Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    System.out.println("A"+i);
                    try {
                        TimeUnit.MICROSECONDS.sleep(500);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    t1.join();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                for (int i = 0; i < 100; i++) {
                    System.out.println("B"+i);
                    try {
                        TimeUnit.MICROSECONDS.sleep(500);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        t1.start();
        t2.start();
    }
}
