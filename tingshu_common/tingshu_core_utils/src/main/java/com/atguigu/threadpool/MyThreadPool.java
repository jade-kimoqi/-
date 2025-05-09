package com.atguigu.threadpool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@EnableConfigurationProperties(MyThreadProperties.class)
@Configuration
public class MyThreadPool {
    @Autowired
    private MyThreadProperties threadProperties;
    /**
     * LinkedBlockingQueue
     * 不会引起空间碎片问题
     * ArrayBlockingQueue
     * 会引起
     */
    @Bean
    public ThreadPoolExecutor myPoolExecutor() {
        return new ThreadPoolExecutor(threadProperties.getCorePoolSize(),
                threadProperties.getMaximumPoolSize(),
                threadProperties.getKeepAliveTime(), TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threadProperties.getQueueLength()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
//    当任务提交时：
//
//    如果当前线程数 < corePoolSize，创建新线程执行任务。
//
//    如果当前线程数 ≥ corePoolSize，任务进入队列等待。
//
//    如果队列已满且线程数 < maximumPoolSize，创建新线程执行任务。
//
//    如果队列已满且线程数 = maximumPoolSize，触发拒绝策略（这里是 CallerRunsPolicy）。
//
//    空闲线程：
//
//    如果线程数 > corePoolSize，且线程空闲时间超过 keepAliveTime，该线程会被终止。
}
