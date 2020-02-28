package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

public interface IOrderService {

    ServerResponse<?> pay(Long orderNo, Integer userId, String path);

    ServerResponse<?> aliCallback(Map<String, String> params);

    ServerResponse<?> queryOrderPayStatus(Integer userId, Long orderNo);

    ServerResponse<?> createOrder(Integer userId, Integer shippingId);

    ServerResponse<?> cancel(Integer userId, Long orderNo);

    ServerResponse<?> getOrderCartProduct(Integer userId);

    ServerResponse<?> getOrderDetail(Integer userId, Long orderNo);

    ServerResponse<?> getOrderList(Integer userId, int pageNum, int pageSize);


    // 后端接口

    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);

    ServerResponse<OrderVo> manageDetail(Long orderNum);

    ServerResponse<PageInfo> manageSearch(Long orderNum, int pageNum, int pageSize);

    ServerResponse<String> manageSendGoods(Long orderNo);
}
