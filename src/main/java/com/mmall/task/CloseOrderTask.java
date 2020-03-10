package com.mmall.task;

import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Spring Schedule 实现定时关单任务
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    // 该方法每分钟执行一次，目的是自动关闭创建了并经过hour（默认是2个小时）个小时未付款的订单
    // TODO: 加上分布式锁 否则多个tomcat集群的环境下每个tomcat都在执行这个定时任务，很容易导致数据错乱（因为大量tomcat都在修改DB数据）和服务器资源浪费
    @Scheduled(cron="0 */1 * * * ?") // 每一分钟（每个一分钟的整数倍的时候执行该方法）
    public void closeOrderTaskV1() {
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }


}
