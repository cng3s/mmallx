package com.mmall.dao;

import com.mmall.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    Order selectByOrderNo(@Param("orderNo") Long orderNo);

    Order selectByUserIdAndOrderNo(@Param("userId") Integer userId, @Param("orderNo") Long orderNo);

    List<Order> selectByUserId(Integer userId);

    List<Order> selectAllOrder(); // 管理员权限用户使用

    // 定时关单
    List<Order> selectOrderStatusByCreateTime(@Param("status") Integer status, @Param("date") String date);

    int closeOrderByOrderId(Integer id);
}