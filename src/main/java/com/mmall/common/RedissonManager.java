package com.mmall.common;


import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Component
@Slf4j
public class RedissonManager {

    private Config config = new Config();

    private Redisson redisson = null;

    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port = Integer.parseInt(
            Objects.requireNonNull(PropertiesUtil.getProperty("redis1.port")));

    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port = Integer.parseInt(
            Objects.requireNonNull(PropertiesUtil.getProperty("redis2.port")));


    // 注：@PostConstruct 意味着首先执行RedissonManager构造器，然后执行init()方法
    @PostConstruct
    private void init() {
        try {
            config.useSingleServer().setAddress(
                    new StringBuilder().append(redis1Ip).append(":").append(redis1Port).toString());
            redisson = (Redisson) Redisson.create(config);
            log.info("初始化Redisson结束");
        } catch (Exception e) {
            log.error("Redisson init error", e);
        }
    }

    public Redisson getRedisson() {
        return redisson;
    }
}
