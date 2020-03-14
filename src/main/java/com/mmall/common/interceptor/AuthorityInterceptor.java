package com.mmall.common.interceptor;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;


@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest
            , HttpServletResponse httpServletResponse, Object handler) throws Exception {

        log.info("preHandle");

        // 请求中Controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 解析HandlerMethod
        String methodName = handlerMethod.getMethod().getName();
        // 注：比较大型复杂的项目的时候，不建议使用getSimpleName()方法，而应该使用getName()方法，因为getName()会返回包名和类名
        // getSimpleName()方法只返回类名，就有可能该类名和其他不同包的类名相同，导致判断出现错误。
        String className = handlerMethod.getBean().getClass().getSimpleName();

        // 解析参数，具体的参数key 以及 value 是什么，我们打印日志
        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = httpServletRequest.getParameterMap();
        Iterator it = paramMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String mapKey = (String) entry.getKey();
            String mapValue = StringUtils.EMPTY;

            // request 这个参数的map，里面的value返回的是一个 String数组
            Object obj = entry.getValue();
            if (obj instanceof String[]) {
                String[] strs = (String[]) obj;
                mapValue = Arrays.toString(strs);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        // 自定义第二种不使用拦截器拦截请求的方法，如果调用 类名 和 方法名 均匹配，则不拦截
        // 这种编写代码的方法 更灵活一些，可以添加自定义的处理。根据实际情况选择第一种或者第二种
        if (StringUtils.equals(className, "UserManageController") && StringUtils.equals(methodName, "login")) {
            log.info("权限拦截器拦截到请求，className:{}, methodName:{}, param:{}"
                    , className, methodName, requestParamBuffer.toString());
            // 如果是拦截器拦截到登录请求，不要打印参数，因为参数中有密码，全部会打印到日志中，防止日志泄露
            return true;
        }

        User user = null;
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);

        if (StringUtils.isNotEmpty(loginToken)) {
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.string2Obj(userJsonStr, User.class);
        }
        if (user == null || (user.getRole() != Const.Role.ROLE_ADMIN)) {
            // 肯定是返回false.即不会调用Controller的方法
            httpServletResponse.reset(); // 这里要添加reset,否则会报异常:getWriter() has already been called for this response
            httpServletResponse.setCharacterEncoding("UTF-8"); // 设置为UTF-8编码，否则会乱码
            httpServletResponse.setContentType("application/json;charset=UTF-8"); // 这里要设置返回值的类型，因为全是json接口

            PrintWriter out = httpServletResponse.getWriter();

            // 上传由于富文本控件的要求，要特殊处理返回值。这里区分是否登录以及是否有权限
            if (user == null) {
                if (StringUtils.equals(className, "ProductManageController")
                        && StringUtils.equals(methodName, "richtextImgUpload")) { // 未登录且调用了上传富文本方法

                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "请登录管理员");
                    out.print(JsonUtil.obj2String(resultMap)); // 将resultMap进行json序列化并返回给前端
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByError("拦截器拦截，用户未登录")));
                }
            } else {
                if (StringUtils.equals(className, "ProductManageController")
                        && StringUtils.equals(methodName, "richtextImgUpload")) { // 非管理员权限用户且调用了上传富文本方法

                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "无权限操作");
                    out.print(JsonUtil.obj2String(resultMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByError("拦截器拦截，用户无权限操作")));
                }
            }

            out.flush();
            out.close(); // 要记住关闭

            return false; // 错误情况，不需要进入Controller
        }

        // 该返回值 true 或 false 决定了是否会进入对应的controller
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest
            , HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

        log.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest
            , HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

        log.info("afterCompletion");
    }
}
