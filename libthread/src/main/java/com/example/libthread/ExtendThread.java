package com.example.libthread;

import java.util.concurrent.TimeUnit;

public class ExtendThread {
    synchronized void m(){
        System.out.println("m start");
        try{
            TimeUnit.SECONDS.sleep(1);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("m end");
    }

    public static void main(String[] args) {
        new ChildT().m();
    }
}

class ChildT extends ExtendThread{
    @Override
    synchronized void m() {
        System.out.println("child m start");
        super.m();
        System.out.println("child m end");
    }
}
