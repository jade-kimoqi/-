package com.atguigu.controller;

import com.atguigu.util.SleepUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Redisson接口")
@RestController
@RequestMapping("/api/album")
public class RedissonController {


@Autowired
private RedissonClient redissonClient;

    /**
     * 默认锁的有效期 internalLockLeaseTime=30*1000 看门狗超时时间lockWatchdogTimeout=30*1000
     * 每隔10s自动续期==看门狗机制 internalLockLeaseTime / 3
     */
    @GetMapping("lock")
    public String lock()  {
        RLock lock = redissonClient.getLock("lock");
        String uuid = UUID.randomUUID().toString();
        try {
            lock.lock();
            //lock.lock();
            //SleepUtils.sleep(60);
            System.out.println(Thread.currentThread().getName()+"执行业务"+uuid);
        } finally {
            lock.unlock();
        }
        return Thread.currentThread().getName()+"执行业务"+uuid;
    }
    //信号量
    @SneakyThrows
    @GetMapping("park")
    public String park()  {
        RSemaphore parkStation = redissonClient.getSemaphore("park_station");
        //信号量减1
        parkStation.acquire(1);
        return Thread.currentThread().getName()+"找到车位";
    }
    //信号量
    @SneakyThrows
    @GetMapping("left")
    public String left()  {
        RSemaphore parkStation = redissonClient.getSemaphore("park_station");
        //信号量加1
        parkStation.release(1);
        return Thread.currentThread().getName()+"离开车位";
    }
    String uuid="";
    @GetMapping("write")
    public String write(){
        RReadWriteLock rwlock = redissonClient.getReadWriteLock("rwLock");
        RLock writeLock = rwlock.writeLock();
        writeLock.lock();
        SleepUtils.sleep(5);
        uuid=UUID.randomUUID().toString();
        writeLock.unlock();
        return Thread.currentThread().getName()+"执行业务"+uuid;
    }

    //读写锁-读
    @GetMapping("read")
    public String read(){
        RReadWriteLock rwlock = redissonClient.getReadWriteLock("rwLock");
        RLock readLock = rwlock.readLock();
        try {
            readLock.lock();
            return uuid;
        } finally {
            readLock.unlock();
        }
    }
    //闭锁
    @SneakyThrows
    @GetMapping("leftRoom")
    public String leftRoom()  {
        RCountDownLatch leftClass = redissonClient.getCountDownLatch("left_class");
        //如果有人走了 数量减1
        leftClass.countDown();
        return Thread.currentThread().getName()+"离开教室";
    }
    //闭锁
    @SneakyThrows
    @GetMapping("lockRoom")
    public String lockRoom()  {
        RCountDownLatch leftClass = redissonClient.getCountDownLatch("left_class");
        //假设班上有6人
        leftClass.trySetCount(6);
        leftClass.await();
        return Thread.currentThread().getName()+"班长离开";
    }

    //公平锁
    @SneakyThrows
    @GetMapping("fairLock/{id}")
    public String fairLock(@PathVariable Long id)  {
        RLock fairLock = redissonClient.getFairLock("fair-lock");
        fairLock.lock();
        SleepUtils.sleep(8);
        System.out.println("公平锁-"+id);
        fairLock.unlock();
        return "公平锁success-"+id;
    }
    //非公平锁
    @SneakyThrows
    @GetMapping("unFairLock/{id}")
    public String unFairLock(@PathVariable Long id)  {
        RLock unFairLock = redissonClient.getLock("unfair-lock");
        unFairLock.lock();
        SleepUtils.sleep(8);
        System.out.println("非公平锁-"+id);
        unFairLock.unlock();
        return "非公平锁success-"+id;
    }


}
