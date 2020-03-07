package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public static final String TOKEN_PREFIX = "token_";

    // Redis 键默认 TTL 时间
    public interface RedisCacheExtime {
        int REDIS_SESSION_EXTIME = 60*30; // ttl = 30min
    }

    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc");
    }

    public interface Cart {
        int CHECKED = 1; // 购物车选中状态
        int UN_CHECKED = 0;

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public interface Role {
        int ROLE_CUSTOMER = 0;
        int ROLE_ADMIN = 1;
    }

    public enum ProductStatusEnum {
        ON_SALE(1, "在线");

        int code;
        String value;

        ProductStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public interface AlipayCallback {
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum PaymentTypeEnum {
        ONLINE_PAY(1, "在线支付");

        private int code;
        private String value;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        PaymentTypeEnum (int code, String value) {
            this.code = code;
            this.value = value;
        }

        public static PaymentTypeEnum codeOf(int code) {
            for (PaymentTypeEnum paymentTypeEnum : PaymentTypeEnum.values()) {
                if (paymentTypeEnum.getCode() == code) {
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("PaymentTypeEnum没有找到对应枚举: " + code);
        }
    }

    public enum PayPlatformEnum {
        ALIPAY(1, "支付宝");

        private int code;
        private String value;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        PayPlatformEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }
    }

    public enum OrderStatusEnum {

        CANCELED(0, "取消"),
        NO_PAY(10, "未支付"),
        PAID(20, "已付款"),
        SHIPPED(40, "已发货"),
        ORDER_SUCCESS(50, "订单完成"),
        ORDER_CLOSED(60, "订单关闭");

        private String value;
        private int code;

        OrderStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public static OrderStatusEnum codeOf(int code) {
            for (OrderStatusEnum orderStatusEnum : OrderStatusEnum.values()) {
                if (orderStatusEnum.getCode() == code) {
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("OrderStatusEnum没有该枚举值: " + code);
        }
    }
}
