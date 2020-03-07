package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.RedisPool;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;


    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session
            , HttpServletResponse httpServletResponse) {
        // controller --> service --> mybatis --> dao
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            // 将LoginToken写入浏览器中，这样相同浏览器相同http header不同标签页访问一个网站就不会出现重复登录的情况
            // 即浏览器为我们保存了cookie信息 ( 注：cookie是保存在客户端的会话状态，session是保存在服务器端的会话状态 )
            CookieUtil.writeLoginToken(httpServletResponse, session.getId());
            RedisShardedPoolUtil.setEx(session.getId()
                    , Const.RedisCacheExtime.REDIS_SESSION_EXTIME, JsonUtil.obj2String(response.getData()));
        }
        return response;
    }

    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        CookieUtil.delLoginToken(httpServletRequest, httpServletResponse);
        RedisShardedPoolUtil.del(loginToken);
        return ServerResponse.createBySuccess();
    }

    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    // 当用户注册时，checkValid实时地返回用户输入信息(用户名、邮箱)合法性，即唯一性
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String val, String type) {
        return iUserService.checkValid(val, type);
    }

    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest httpServletRequest) {


        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        log.info("getUserInfo, loginToken: " + loginToken);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        log.info("getUserInfo, userJsonStr: " + userJsonStr);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        log.info("getUserInfo, user: " + user);
        if (user != null) {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
    }

    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        // 为什么这里不需要HttpSession session作为参数？
        // 因为forget_get_question不需要获取当前用户信息，获取忘记密码问题不需要用户登录，也就不需要HttpSession
        // 在该项目中，只有需要用户登录才能进行的操作的方法才需要HttpSession操作
        // 更严谨的说，是需要HttpSession的信息的时候才需要session作为参数
        return iUserService.selectQuestion(username);
    }

    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }

    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String token) {
        return iUserService.forgetResetPassword(username, newPassword, token);
    }

    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(
            HttpServletRequest httpServletRequest, String oldPassword, String newPassword) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录");
        }
        return iUserService.resetPassword(oldPassword, newPassword, user);
    }

    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpServletRequest httpServletRequest, User user) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(userJsonStr, User.class);
        if (currentUser == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        // 用户不能改变其用户ID和用户名，所以更新时前端不需要传递这两个参数信息，而是靠后端给予
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()) {
            response.getData().setUsername(currentUser.getUsername());
            // 如果更新成功，只需要把session中CURRENT_USER域设置为更新用户信息
            RedisShardedPoolUtil.setEx(
                    loginToken, Const.RedisCacheExtime.REDIS_SESSION_EXTIME, JsonUtil.obj2String(response.getData()));
        }
        return response;
    }

    // get_information和getUserInfo区别在前者获取的是数据库中该用户的全部信息，后者只获取session中的用户信息
    // 当前端获取ResponseCode.NEED_LOGIN时跳转到登录页面
    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpServletRequest httpServletRequest) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }
        return iUserService.getInformation(user.getId());
    }
}
