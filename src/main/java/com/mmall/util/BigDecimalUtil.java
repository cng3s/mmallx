package com.mmall.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {

    public static BigDecimal add(double x, double y) {
        BigDecimal b1 = new BigDecimal(Double.toString(x));
        BigDecimal b2 = new BigDecimal(Double.toString(y));
        return b1.add(b2);
    }

    public static BigDecimal sub(double x, double y) {
        BigDecimal b1 = new BigDecimal(Double.toString(x));
        BigDecimal b2 = new BigDecimal(Double.toString(y));
        return b1.subtract(b2);
    }

    public static BigDecimal mul(double x, double y) {
        BigDecimal b1 = new BigDecimal(Double.toString(x));
        BigDecimal b2 = new BigDecimal(Double.toString(y));
        return b1.multiply(b2);
    }

    public static BigDecimal div(double x, double y) {
        BigDecimal b1 = new BigDecimal(Double.toString(x));
        BigDecimal b2 = new BigDecimal(Double.toString(y));
        return b1.divide(b2, 2, RoundingMode.HALF_UP); // 保留两位小数且四舍五入
    }
}
