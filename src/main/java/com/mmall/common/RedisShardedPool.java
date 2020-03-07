package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RedisShardedPool {

    private static ShardedJedisPool pool;
    // 最大连接池
    private static Integer maxTotal = Integer.parseInt(
            PropertiesUtil.getProperty("redis.max.total", "20"));

    // jedispool中最大的idle状态(空闲状态）的jedis实例数
    private static Integer maxIdle = Integer.parseInt(
            PropertiesUtil.getProperty("redis.max.idle", "20"));

    // jedispool中最小的idle状态(空闲状态）的jedis实例数
    private static Integer minIdle = Integer.parseInt(
            PropertiesUtil.getProperty("redis.min.idle", "20"));

    // 在borrow一个jedis实例的时候，是否要进行验证操作
    // 如果赋值为true，则得到的jedis实例肯定可以使用
    private static Boolean testOnBorrow = Boolean.parseBoolean(
            PropertiesUtil.getProperty("redis.test.borrow", "true"));

    // 在return一个jedis实例的时候，是否要进行验证操作
    // 如果赋值为true，则放回jedispool的jedis实例肯定可以使用
    private static Boolean testOnReturn = Boolean.parseBoolean(
            PropertiesUtil.getProperty("redis.test.return", "true"));

    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");

    private static Integer redis1Port = Integer.parseInt(
            Objects.requireNonNull(PropertiesUtil.getProperty("redis1.port")));


    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip");

    private static Integer redis2Port = Integer.parseInt(
            Objects.requireNonNull(PropertiesUtil.getProperty("redis2.port")));

    public static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        // 连接耗尽的时候是否阻塞
        // 如果设为false会直接抛出阻塞异常，true则阻塞直到超时后会抛出超时异常。
        // 默认为true。不设置也可以。
        config.setBlockWhenExhausted(true);

        // 这里timeout单位是ms
        JedisShardInfo info1 = new JedisShardInfo(redis1Ip, redis1Port, 1000*2); // ttl timeout=2s

        JedisShardInfo info2 = new JedisShardInfo(redis2Ip, redis2Port, 1000*2); // ttl timeout=2s

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>(2);
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        // 默认的ShardedJedisPool就是MURMUR_HASH,对应的就是一致性算法
        pool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);

    }

    static {
        initPool();
    }

    // 外部获取RedisPool中的jedis实例
    public static ShardedJedis getJedis() {
        return pool.getResource();
    }


    // 把坏连接放到jedis pool中的broken resource连接池中
    public static void returnBrokenResource(ShardedJedis jedis) {
        // returnResource代码中已经有判断 jedis 不空才操作，所以这里不需要
        pool.returnBrokenResource(jedis);
    }

    // 把jedis实例返回resource连接池
    public static void returnResource(ShardedJedis jedis) {
        // returnResource代码中已经有判断 jedis 不空才操作，所以这里不需要
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();

        for (int i = 0; i < 10; i++) {
            jedis.set("key"+i, "value"+i);
        }
        returnResource(jedis);

        // 销毁连接池中所有连接
        // 临时调用，平时不要调用这段代码来销魂redis连接池
//        pool.destroy();

        System.out.println("program is end");
    }
}
