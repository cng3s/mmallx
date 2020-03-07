package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {
    // 品类操作通常不具有删除的方法
    // 原因在于品类删除可能会影响到商品的信息，产生脏数据

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse<?> addCategory(HttpServletRequest httpServletRequest, String categoryName
            , @RequestParam(value = "parentId", defaultValue = "0") int parentId) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.addCategory(categoryName, parentId);
        }
        return ServerResponse.createByError(ResponseCode.NEED_ADMIN.getCode(), ResponseCode.NEED_ADMIN.getDesc());
    }

    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse<?> setCategoryName(
            HttpServletRequest httpServletRequest, Integer categoryId, String categoryName) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        }
        return ServerResponse.createByError(ResponseCode.NEED_ADMIN.getCode(), ResponseCode.NEED_ADMIN.getDesc());
    }

    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse<?> getChildrenParallelCategory(HttpServletRequest httpServletRequest
            , @RequestParam(value = "categoryId", defaultValue = "0") int categoryId) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }
        return ServerResponse.createByError(ResponseCode.NEED_ADMIN.getCode(), ResponseCode.NEED_ADMIN.getDesc());
    }

    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse<?> getCategoryAndDeepChildrenCategory(HttpServletRequest httpServletRequest
            , @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
        }

       if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
       }
       return ServerResponse.createByError(ResponseCode.NEED_ADMIN.getCode(), ResponseCode.NEED_ADMIN.getDesc());
    }
}
