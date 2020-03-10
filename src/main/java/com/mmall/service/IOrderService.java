package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

public interface IOrderService {

    // 前台接口
    ServerResponse<?> pay(Long orderNo, Integer userId, String path);

    ServerResponse<?> aliCallback(Map<String, String> params);

    ServerResponse<?> queryOrderPayStatus(Integer userId, Long orderNo);

    ServerResponse<?> createOrder(Integer userId, Integer shippingId);

    ServerResponse<?> cancel(Integer userId, Long orderNo);

    ServerResponse<?> getOrderCartProduct(Integer userId);

    ServerResponse<?> getOrderDetail(Integer userId, Long orderNo);

    ServerResponse<?> getOrderList(Integer userId, int pageNum, int pageSize);


    // 后台接口
    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);

    ServerResponse<OrderVo> manageDetail(Long orderNum);

    ServerResponse<PageInfo> manageSearch(Long orderNum, int pageNum, int pageSize);

    ServerResponse<String> manageSendGoods(Long orderNo);

    // Spring Schedule 自动化任务
    // hour个小时以内未付款的订单，进行关闭
    void closeOrder(int hour);
}
