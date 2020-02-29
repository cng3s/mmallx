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


    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        // controller --> service --> mybatis --> dao
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
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
    public ServerResponse<User> getUserInfo(HttpSession session) {
        // 为什么通过CURRENT_USER属性就能获取User对象信息？
        // 因为login里session中设置为Const.CURRENT_USER，所以通过session的getAttribute方法可以获取
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByError("用户未登录");
        }
        return ServerResponse.createBySuccess(user);
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
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String tok) {
        return iUserService.forgetResetPassword(username, newPassword, tok);
    }

    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(
            HttpSession session, String oldPassword, String newPassword) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServerResponse.createByError("用户未登录");
        return iUserService.resetPassword(oldPassword, newPassword, user);
    }

    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session, User user) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByError("用户未登录");
        }
        user.setId(currentUser.getId());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()) {
            // 用户不能改变其用户ID和用户名，所以更新时前端不需要传递这两个参数信息，而是靠后端给予
            User updatedUser = response.getData();
            updatedUser.setUsername(currentUser.getUsername());
            // 如果更新成功，只需要把session中CURRENT_USER域设置为更新用户信息
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    // get_information和getUserInfo区别在前者获取的是数据库中该用户的全部信息，后者只获取session中的用户信息
    // 当前端获取ResponseCode.NEED_LOGIN时跳转到登录页面
    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iUserService.getInformation(currentUser.getId());
    }
}
