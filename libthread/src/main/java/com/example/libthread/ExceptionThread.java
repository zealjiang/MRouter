package com.example.libthread;

import java.util.concurrent.TimeUnit;

public class ExceptionThread {
    int count = 0;
    synchronized void m(){
        System.out.println(Thread.currentThread().getName()+" start");
        while (true){
            count++;
            System.out.println(Thread.currentThread().getName()+" count ="+count);
            try {
                TimeUnit.SECONDS.sleep(1);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            if(count == 5){
                int i = 1/0;
                System.out.println(i);
            }
        }
    }

    static {
        System.out.println("--static block---");
    }
    public static void main(String[] args) {
        System.out.println("--main---");
        final ExceptionThread exceptionThread = new ExceptionThread();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                exceptionThread.m();
            }
        };

        new Thread(r,"t1").start();
        try {
            TimeUnit.SECONDS.sleep(3);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        new Thread(r,"t2").start();
    }
}