package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/order/")
public class OrderController {

    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse<?> create(HttpServletRequest httpServletRequest, Integer shippingId) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
            if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
            if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }
        return iOrderService.createOrder(user.getId(), shippingId);
    }

    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse<?> pay(HttpServletRequest httpServletRequest, Long orderNo) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }
        String path = httpServletRequest.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(orderNo, user.getId(), path);
    }

    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse<?> cancel(HttpServletRequest httpServletRequest, Long orderNo) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }
        return iOrderService.cancel(user.getId(), orderNo);
    }

    // 获取购物车中已经选中得商品信息
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse<?> getOrderCartProduct(HttpServletRequest httpServletRequest) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }

    // 获取订单详情信息
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<?> detail(HttpServletRequest httpServletRequest, Long orderNo) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }
        return iOrderService.getOrderDetail(user.getId(), orderNo);
    }

    // 获取用户所有订单
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<?> list(HttpServletRequest httpServletRequest
            , @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum
            , @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }
        return iOrderService.getOrderList(user.getId(), pageNum, pageSize);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        Map<String, String> params = Maps.newHashMap();
        Map requestParams = request.getParameterMap();
        for (Object o : requestParams.keySet()) {
            String name = (String) o;
            String[] values = (String[]) requestParams.get(name);
            String valStr = "";
            for (int i = 0; i < values.length; i++) {
                valStr = (i == values.length-1) ? valStr + values[i] : valStr + values[i] + ",";
            }
            params.put(name, valStr);
        }
        log.info("支付宝回调: sign:{},trade_status:{},param:{}"
                , params.get("sign"), params.get("trade_status"), params.toString());
        // 非常重要，验证回调正确性，是不是支付宝发的信息，而且要避免重复通知
        params.remove("sign_type");
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params
                    , Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if (!alipayRSACheckedV2) {
                return ServerResponse.createByError("非法请求，验证不通过。服务器已记录该请求。如果继续请求可能会交移网警处理。");
            }
        } catch (AlipayApiException e) {
            log.error("支付宝回调异常", e);
        }

        // TODO 验证各种数据

        ServerResponse<?> serverResponse = iOrderService.aliCallback(params);
        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }


    // 查询订单支付状态是否成功
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpServletRequest httpServletRequest, Long orderNo) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        ServerResponse<?> response = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if (response.isSuccess()) {
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }
}
