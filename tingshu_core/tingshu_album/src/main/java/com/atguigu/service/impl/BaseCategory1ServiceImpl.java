package com.atguigu.service.impl;

import com.atguigu.entity.BaseCategory1;
import com.atguigu.mapper.BaseCategory1Mapper;
import com.atguigu.service.BaseCategory1Service;
import com.atguigu.util.SleepUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.common.util.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 一级分类表 服务实现类
 * </p>
 *
 * @author long
 * @since 2025-03-07
 */
@Service
public class BaseCategory1ServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategory1Service {
@Autowired
private RedisTemplate redisTemplate;
//    @Override
    public void setNum01() {
        String num =(String) redisTemplate.opsForValue().get("num");
        if(StringUtils.isEmpty(num)){
            redisTemplate.opsForValue().set("num","1");
        }else {
            int value = Integer.parseInt(num);
            redisTemplate.opsForValue().set("num",String.valueOf(++value));
        }
    }
    private void doBusiness() {
        String num = (String) redisTemplate.opsForValue().get("num");
        if (StringUtils.isEmpty(num)) {
            redisTemplate.opsForValue().set("num", "1");
        } else {
            int value = Integer.parseInt(num);
            redisTemplate.opsForValue().set("num", String.valueOf(++value));
        }
    }
    Map<Thread, String> threadMap1 = new HashMap();
    ThreadLocal<String> threadLocal = new ThreadLocal<>();

//    @Override
    public void setNum02() {
        String token = threadLocal.get();
        boolean accquireLock = false;
        if (!StringUtils.isEmpty(token)) {
            accquireLock = true;
        } else {
            token = UUID.randomUUID().toString();
            accquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token, 3, TimeUnit.HOURS);
        }
        if (accquireLock) {
            doBusiness();
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(luaScript);
            redisScript.setResultType(Long.class);
            redisTemplate.execute(redisScript, Arrays.asList("lock"), token);
            //擦屁股
            threadLocal.remove();
        } else {
            while (true) {
                SleepUtils.millis(50);
                boolean retryAccquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token, 3, TimeUnit.HOURS);
                if (retryAccquireLock) {
                    threadLocal.set(token);
                    break;
                }
            }
            setNum();
        }
    }
    @Autowired
    private RedissonClient redissonClient;
    @Override
    public void setNum(){
        RLock lock = redissonClient.getLock("lock");
        lock.lock();
        doBusiness();
        lock.unlock();

    }
}
