package com.example.libthread;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CountDownLatchTest {

    public static void main(String[] args) {
        CountDownLatchTest countDownLatchTest = new CountDownLatchTest();
        countDownLatchTest.countDownLatchTest();
    }

    private void countDownLatchTest(){
        ExecutorService executor = Executors.newCachedThreadPool();

        CountDownLatch latch = new CountDownLatch(3);

        Worker w1 = new Worker(latch,"张三");
        Worker w2 = new Worker(latch,"李四");
        Worker w3 = new Worker(latch,"王二");

        Boss boss = new Boss(latch);

        executor.execute(w3);
        executor.execute(w2);
        executor.execute(w1);
/*        try{
            TimeUnit.SECONDS.sleep(new Random().nextInt(10));
        }catch(InterruptedException ie){
        }*///此处模拟让工人都把活先干完了，此时countdown为0，老板才调用await()，会不会进就等待的情况
        executor.execute(boss);

        executor.shutdown();
    }


    class Worker implements Runnable{

        private CountDownLatch downLatch;
        private String name;

        public Worker(CountDownLatch downLatch, String name){
            this.downLatch = downLatch;
            this.name = name;
        }

        public void run() {
            this.doWork();
            try{
                TimeUnit.SECONDS.sleep(new Random().nextInt(10));
            }catch(InterruptedException ie){
            }//此处模拟让工人都把活先干完了，此时countdown为0，老板才调用await()，会不会进就等待的情况
            System.out.println(this.name + "活干完了！");
            this.downLatch.countDown();
        }

        private void doWork(){
            System.out.println(this.name + "正在干活!");
        }

    }

    class Boss implements Runnable {

        private CountDownLatch downLatch;

        public Boss(CountDownLatch downLatch){
            this.downLatch = downLatch;
        }

        public void run() {
            System.out.println("老板正在等所有的工人干完活......");
            try {
                this.downLatch.await();
            } catch (InterruptedException e) {
            }
            System.out.println("工人活都干完了，老板开始检查了！");
        }

    }
}
