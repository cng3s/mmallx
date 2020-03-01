package com.mmall.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Integer id;

    private Long orderNo; // 订单号

    private Integer userId; // 用户

    private Integer shippingId; // 交易号

    private BigDecimal payment; // 订单价格

    private Integer paymentType; // 付款方式

    private Integer postage; // 邮费

    private Integer status; // 交易状态

    private Date paymentTime; // 支付时间

    private Date sendTime; // 发货时间

    private Date endTime; // 交易完成时间

    private Date closeTime; // 关闭交易时间

    private Date createTime; // 创建订单时间

    private Date updateTime; // 更新订单时间
}