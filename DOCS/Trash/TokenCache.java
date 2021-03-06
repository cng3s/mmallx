package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

// TokenCache 被 RedisPool 代替
// 原因是因为多tomcat环境下无法使用 TokenCache
// TokenCache只能保存该tomcat下的token cache信息
// 而其他的tomcat服务器不能访问到这个 tomcat 下 token cache信息
// 所以使用 redis 服务器作为新的 token cache
@Slf4j
public class TokenCache {

    public static final String TOKEN_PREFIX = "token_";

    // 使用LRU算法
    private static LoadingCache<String, String> localCache =
            CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000)
                    .expireAfterAccess(12, TimeUnit.HOURS)
                    .build(new CacheLoader<String, String>() {
                        // 默认的数据加载实现，当调用get取值的时候，如果key没有对应值，就用该匿名方法加载
                        @Override
                        public String load(String s) throws Exception {
                            return "null";
                        }
                    });

    public static void setKey(String key, String value) {
        localCache.put(key, value);
    }

    public static String getKey(String key) {
        try {
            String val = localCache.get(key);
            if ("null".equals(val)) {
                return null;
            }
            return val;
        } catch (Exception e) {
            log.error("localCache get error", e);
        }
        return null;
    }

}
