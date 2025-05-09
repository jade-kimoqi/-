package com.test;

import com.atguigu.util.SleepUtils;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class demo1 {
    public static void main(String[] args) {
        supplyAsync();
        System.out.println(Thread.currentThread().getName()+"over了");
    }
@SneakyThrows
    private static void supplyAsync() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                System.out.println(Thread.currentThread().getName()+"hello supplyAsync");
                SleepUtils.sleep(2);
                System.out.println(Thread.currentThread().getName()+"?????");
                return "学习completableFuture";
            }
        });
        System.out.println(future.get());
    }
    //发起一个异步请求
    public static void runAsync() {
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName()+"你好runAsync");
                SleepUtils.sleep(10);
                System.out.println(Thread.currentThread().getName()+"runAsync");
            }
        });

    }
}
