package com.test;

import com.atguigu.util.SleepUtils;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class demo2 {
    public static void main(String[] args) {
        runAsync();
        System.out.println(Thread.currentThread().getName()+"over了");
        SleepUtils.sleep(2);
    }
@SneakyThrows

    //发起一个异步请求
    public static void runAsync() {
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName()+"你好runAsync");
//                int i = 1/0;
            }
        }).whenComplete(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void acceptVal, Throwable throwable) {
                System.out.println("执行异步请求成功之后的接收值:"+acceptVal);
                System.out.println("whenComplete异步请求过程中发生的异常:"+throwable);
            }
        }).exceptionally(new Function<Throwable, Void>() {
            @Override
            public Void apply(Throwable throwable) {
                System.out.println("exceptionally异步请求过程中发生的异常:"+throwable);
                return null;
            }
        });

    }
}
