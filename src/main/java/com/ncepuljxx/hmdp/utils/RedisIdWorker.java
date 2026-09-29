package com.ncepuljxx.hmdp.utils;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    /**
     * 开始时间戳
     */
    private static final long BEGIN_TIMESTAMP = 1767225600;

    /**
     * 序列号位数
     */
    private static final long COUNT_BITS = 32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String keyPrefix) {
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 2.生成序列号
        // 2.1.获取日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyy::MM:dd"));
        // 2.2.自增长
        long count = stringRedisTemplate.opsForValue().increment(" icr:" + keyPrefix + ":" + date);
        stringRedisTemplate.opsForValue().increment(keyPrefix, timestamp);

        // 3.拼接并返回
        return timestamp << COUNT_BITS | count;
    }

    // 计算初始时间
    /* public static void main(String[] args) {
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        long second = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println(second);
    }*/
}
