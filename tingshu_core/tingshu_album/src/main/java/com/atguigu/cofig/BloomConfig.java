package com.atguigu.cofig;

import com.atguigu.constant.RedisConstant;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomConfig {
    @Autowired
    private RedissonClient redissonClient;
    @Bean
    public RBloomFilter albumBloomFilter(){
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstant.ALBUM_BLOOM_FILTER);
//        初始化容量和容错率
        bloomFilter.tryInit(10000,0.001);
        return bloomFilter;
    }
}
