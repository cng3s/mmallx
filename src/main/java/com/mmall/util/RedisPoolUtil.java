package com.mmall.util;

import com.mmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.IOException;

@Slf4j
public class RedisPoolUtil {

    // redis set
    public static String set(String key, String value) {
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.set(key,value);
        } catch (Exception e) {
            // 也可使用e.getMessage()，但这个只提供错误位置，错误信息提供得很少
            // 所以这里直接使用 e 打印具体信息
            log.error("set key: {}, value {}, error {}", key, value, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    // redis get
    public static String get(String key) {
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("get key: {}, error: {}", key, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    // redis setEx - 设置带ttl的键值对
    // exTime: 设置ttl(s)
    public static String setEx(String key, int exTime, String value) {
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.setex(key, exTime, value);
        } catch (Exception e) {
            log.error("get key: {}, value: {}, error: {}", key, value, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    // redis expire - 重新设置键的ttl(s)
    // public static Long expire (String key, int exTime) { ... }
    public static Long expire(String key, int exTime) {
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.expire(key, exTime);
        } catch (Exception e) {
            log.error("get key: {}, error: {}", key, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    // redis del - 删除一个key
    public static Long del(String key) {
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("get key: {}, error: {}", key, e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static void main(String[] args) {
        Jedis jedis = RedisPool.getJedis();
        RedisPoolUtil.set("keyTest", "valueTest11988");
        String value = RedisPoolUtil.get("keyTest");
        System.out.println(value);
        RedisPoolUtil.setEx("keyex", 60*10, "valueex"); // set ttl 10min
        RedisPoolUtil.expire("keyTest",60*20); // set ttl 20min

        System.out.println("end");
    }
}
