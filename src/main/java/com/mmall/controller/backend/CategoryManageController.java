package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public ServerResponse<?> addCategory(HttpSession session, String categoryName
            , @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.addCategory(categoryName, parentId);
        }
        return ServerResponse.createByError(ResponseCode.NEED_ADMIN.getCode(), ResponseCode.NEED_ADMIN.getDesc());
    }

    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse<?> setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        }
        return ServerResponse.createByError(ResponseCode.NEED_ADMIN.getCode(), ResponseCode.NEED_ADMIN.getDesc());
    }

    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse<?> getChildrenParallelCategory(HttpSession session
            , @RequestParam(value = "categoryId", defaultValue = "0") int categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }
        return ServerResponse.createByError(ResponseCode.NEED_ADMIN.getCode(), ResponseCode.NEED_ADMIN.getDesc());
    }

    // 重要
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse<?> getCategoryAndDeepChildrenCategory(HttpSession session
            , @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }
        return ServerResponse.createByError(ResponseCode.NEED_ADMIN.getCode(), ResponseCode.NEED_ADMIN.getDesc());
    }
}
