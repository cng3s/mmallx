package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService") // 将IUserService注入到Controller中并叫做iUserService
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int result_cnt = userMapper.checkUsername(username);
        if (result_cnt == 0)
            return ServerResponse.createByError("用户名不存在");

        String md5_passwd = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5_passwd);
        if (user == null)
            return ServerResponse.createByError("密码错误");
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> check_res = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!check_res.isSuccess())
            return check_res;
        check_res = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!check_res.isSuccess())
            return check_res;

        user.setRole(Const.Role.ROLE_CUSTOMER);
        String md5_passwd = MD5Util.MD5EncodeUtf8(user.getPassword());
        System.out.println("Register MD5Password: " + md5_passwd);
        user.setPassword(md5_passwd);
        int result_cnt = userMapper.insert(user);
        if (result_cnt == 0)
            return ServerResponse.createByError("注册失败");
        return ServerResponse.createBySuccess("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String val, String type) {
        if (StringUtils.isBlank(type))
            return ServerResponse.createByError("参数错误");

        // 假设type要么等于username要么等于email
        if (Const.USERNAME.equals(type)) {
            int result_cnt = userMapper.checkUsername(val);
            if (result_cnt > 0)
                return ServerResponse.createByError("用户名已存在");
        } else if (Const.EMAIL.equals(type)) {
            int result_cnt = userMapper.checkEmail(val);
            if (result_cnt > 0)
                return ServerResponse.createByError("email已存在");
        }
        return ServerResponse.createBySuccess("校验成功");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse<String> check = this.checkValid(username, Const.USERNAME);
        if (check.isSuccess())
            return ServerResponse.createByError("用户不存在");
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isBlank(question))
            return ServerResponse.createByError("用户问题为空");
        return ServerResponse.createBySuccess(question);
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int res_cnt = userMapper.checkAnswer(username, question, answer);
        if (res_cnt == 0)
            return ServerResponse.createByError("用户回答错误");

        // 使用UUID生成重复概率极小的字符串作为token
        String forget_tok = UUID.randomUUID().toString();
        TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forget_tok);
        return ServerResponse.createBySuccess(forget_tok);
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String newpasswd, String tok) {
        System.out.println("Enter forgetResetPassword Service");
        if (StringUtils.isBlank(tok))
            return ServerResponse.createByError("参数错误，token需要传递");
        ServerResponse<String> check = checkValid(username, Const.USERNAME);
        if (check.isSuccess())
            return ServerResponse.createByError("用户不存在");
        String curtoken = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(curtoken))
            return ServerResponse.createByError("token无效或过期");
        System.out.println("Check error parameters done!");
        if (StringUtils.equals(tok, curtoken)) {
            String md5passwd = MD5Util.MD5EncodeUtf8(newpasswd);
            System.out.println("forgetResetPassword : " + md5passwd);
            int row_cnt = userMapper.updatePasswordByUsername(username, md5passwd);
            System.out.println("done!!!!");
            if (row_cnt > 0)
                return ServerResponse.createBySuccess("修改密码成功");
        } else {
            return ServerResponse.createByError("token错误，请重新获取修改密码的token");
        }
        return ServerResponse.createByError("修改密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String oldpasswd, String newpasswd, User user) {
        // 为了防止横向越权，要校验用户密码，一定要指定是这个用户
        // 因为sql语句将会查询count(1),如果不指定id,而单验证oldpasswd,结果很大概率是true
        // 因为mmall_user中会保存很多不同用户但设置完全相同的密码
        // 这样攻击者就会对该密码尝试进行攻击
        int res_cnt = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldpasswd), user.getId());
        if (res_cnt == 0)
            return ServerResponse.createByError("用户密码不正确");
        user.setPassword(MD5Util.MD5EncodeUtf8(newpasswd));
        int update_cnt = userMapper.updateByPrimaryKeySelective(user);
        if (update_cnt > 0)
            return ServerResponse.createBySuccess("密码更新成功");
        return ServerResponse.createByError("密码更新失败");
    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        // id, username不能被更新, 更新email时需校验该email地址是否被使用
        int check_cnt = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (check_cnt > 0)
            return ServerResponse.createByError("email已被占用");
        User update_user = new User();
        update_user.setId(user.getId());
        update_user.setRole(user.getRole());
        update_user.setEmail(user.getEmail());
        update_user.setPhone(user.getPhone());
        update_user.setQuestion(user.getQuestion());
        update_user.setAnswer(user.getAnswer());

        int update_cnt = userMapper.updateByPrimaryKeySelective(update_user);
        if (update_cnt > 0)
            return ServerResponse.createBySuccess("更新成功", update_user);
        return ServerResponse.createByError("更新失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null)
            return ServerResponse.createByError("找不到当前用户");
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN)
            return ServerResponse.createBySuccess();
        return ServerResponse.createByError();
    }
}
