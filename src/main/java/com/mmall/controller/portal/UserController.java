package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;




    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @return ServerResponse<User> 返回响应请求
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        // controller --> service --> mybatis --> dao
        System.out.println("Login Controller Username: " + username);
        System.out.println("Login Controller Password: " + password);
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess())
            session.setAttribute(Const.CURRENT_USER, response.getData());
        return response;
    }

    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess("注销成功");
    }

    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user)
    { return iUserService.register(user); }

    /**
     * 当用户注册时，checkValid实时地返回用户输入信息(用户名、邮箱)合法性，即唯一性
     * @param val: 用户输入的内容
     * @param type: 传入要判断的内容是username还是email
     */
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String val, String type)
    { return iUserService.checkValid(val, type); }

    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        // 为什么通过CURRENT_USER属性就能获取User对象信息？
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServerResponse.createByError("用户未登录");
        return ServerResponse.createBySuccess(user);
    }

    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username)
    { return iUserService.selectQuestion(username); }

    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer)
    { return iUserService.checkAnswer(username, question, answer); }

    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String tok)
    {
        System.out.println("Controller forgetResetPassword"); return iUserService.forgetResetPassword(username, newPassword, tok); }

    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(
            HttpSession session, String oldPassword, String newPassword) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServerResponse.createByError("用户未登录");
        return iUserService.resetPassword(oldPassword, newPassword, user);
    }

    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session, User user) {
        User curuser = (User) session.getAttribute(Const.CURRENT_USER);
        if (curuser == null)
            return ServerResponse.createByError("用户未登录");
        user.setId(curuser.getId());
        user.setUsername(curuser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()) {
            response.getData().setUsername(curuser.getUsername());
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * get_information和getUserInfo区别在前者获取的是数据库中用户全部信息，后者只获取session中的用户信息
     * 当前端获取status=10时跳转到登录页面
     * */
    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session) {
        User curuser = (User)session.getAttribute(Const.CURRENT_USER);
        if (curuser == null)
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要强制登录status=10");
        return iUserService.getInformation(curuser.getId());
    }
}
