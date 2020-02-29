package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    public ServerResponse<String> addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByError(
                    ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int res_cnt = categoryMapper.insert(category);
        if (res_cnt > 0) {
            return ServerResponse.createBySuccess(ResponseCode.SUCCESS.getDesc());
        }
        return ServerResponse.createByError(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
    }

    public ServerResponse<String> updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByError(
                    ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int res_cnt = categoryMapper.updateByPrimaryKeySelective(category);
        if (res_cnt > 0) {
            return ServerResponse.createBySuccess("更新品类名成功");
        }
        return ServerResponse.createByError("更新品类名失败");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) { // 不论有无找到子分类，都返回createBySuccess
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet, categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null) {
            for (Category item : categorySet)
                categoryIdList.add(item.getId());
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    // 递归地获取所有子品类


    private void findChildCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category item : categoryList) {
            findChildCategory(categorySet, item.getId());
        }
    }


}

    /* BUG方法
    private void findChildCategory11(Set<Category> categorySet, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category == null) {
        // 这里有bug , categoryId == 0时category == null
        // 但是根分类节点的parent_id==0，但无法检索出来
            return;
        }
        categorySet.add(category);
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category item : categoryList) {
            findChildCategory(categorySet, item.getId());
        }
    }*/
