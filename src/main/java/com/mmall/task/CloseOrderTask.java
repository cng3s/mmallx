package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

// Spring Schedule 实现定时关单任务
// PS: 很多错误实现例子存在这里
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

//    @Autowired
//    private RedissonManager redissonManager;

    // @PreDestroy在使用tomcat shutdown程序关闭tomcat时会执行这个函数并释放redis分布式锁
    // 但是如果直接通过任务管理器 kill 掉进程，则不会释放redis分布式锁。
    // 并且当分布式锁非常之多的情况下，关闭tomcat需要的时间会非常长
    @PreDestroy
    public void delLock() {
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    }

    // 该方法每分钟执行一次，目的是自动关闭创建了经过hour（默认是2个小时）个小时未付款的订单
    // 加上分布式锁 否则多个tomcat集群的环境下每个tomcat都在执行这个定时任务
    // 很容易导致数据错乱（因为大量tomcat都在修改DB数据）和服务器资源浪费
//    @Scheduled(cron="0 */1 * * * ?") // 每一分钟（每个一分钟的整数倍的时候执行该方法）
    public void closeOrderTaskV1() {
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

    // 这段代码有严重的问题，如果程序在执行完 setnx 之后突然崩溃，导致锁没有设置过期时间，则redis分布式锁根本不会被释放，就彻底锁死了，即发生死锁
    // 其原因在于我们先做 setnx 判断，然后再做 expire设置， 这两个操作结合不具有原子性，所以会发生错误
    // 折中方案，delLock(); 更好的方案：closeOrderTaskV3()
//    @Scheduled(cron="0 */1 * * * ?") // 每一分钟（每个一分钟的整数倍的时候执行该方法）
    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务启动");
        long lockTimeOut = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "2"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK
                , String.valueOf(System.currentTimeMillis()+lockTimeOut));

        if (setnxResult != null && setnxResult.intValue() == 1) {
            // 如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            log.info("没有获得分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }

    // 分布式锁, 但其实这个实现仍然是错误的，而且错误原因和上面的一模一样，即操作不具有原子性
    @Scheduled(cron="0 */1 * * * ?") // 每一分钟（每个一分钟的整数倍的时候执行该方法）
    public void closeOrderTaskV3() {
        log.info("关闭订单定时任务启动");

        long lockTimeOut = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "2"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK
                , String.valueOf(System.currentTimeMillis()+lockTimeOut));

        if (setnxResult != null && setnxResult.intValue() == 1) {
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            // 未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                String getSetResult = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK
                                                , String.valueOf(System.currentTimeMillis()+lockTimeOut));

                // 再次用时间戳getset。
                // 返回给定的key的旧值 -> 旧值判断，是否可以获取锁
                // 当key没有旧值时，即key不存在时，返回nil
                // 这里我们set了一个新的value值，获取旧的值
                if (getSetResult == null || StringUtils.equals(lockValueStr, getSetResult)) {
                    // 真正获取到锁
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                } else {
                    log.info("没有获取到分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            } else {
                log.info("没有获取到分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }

        log.info("关闭订单定时任务结束");
    }

    // Redisson 2.9.0 不能使用
//    // 这个版本使用Redisson搞定
//    @Scheduled(cron="0 */1 * * * ?") // 每一分钟（每个一分钟的整数倍的时候执行该方法）
//    public void closeOrderTaskV4() {
//        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
//        boolean getLock = false;
//        try {
//            if (getLock = lock.tryLock(2, 5, TimeUnit.SECONDS)) {
//                log.info("Redisson获取分布式锁: {}, ThreadName: {}"
//                        , Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
//                int hour = Integer.parseInt(Objects.requireNonNull(PropertiesUtil.getProperty("close.order.task.time.hour")));
////                iOrderService.closeOrder(hour);
//            } else {
//                log.info("Redisson没有获取分布式锁: {}, ThreadName: {}"
//                        , Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
//            }
//        } catch (InterruptedException e) {
//            log.info("Redisson分布式锁获取异常: {}, ThreadName: {}"
//                    , Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
//        } finally {
//            if (!getLock) {
//                return;
//            }
//            lock.unlock();
//            log.info("Redisson分布式锁释放锁");
//        }
//    }


    private void closeOrder(String lockName) {
        RedisShardedPoolUtil.expire(lockName, 50); // 有效期50秒，防止死锁

        // 打印出哪个线程获取了锁
        log.info("获取{}, ThreadName: {}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task", "2"));
        iOrderService.closeOrder(hour);
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放{}, ThreadName: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        log.info("================================================================================================");
    }
}
