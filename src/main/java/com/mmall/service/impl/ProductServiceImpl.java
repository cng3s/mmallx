package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    public ServerResponse<String> saveOrUpdateProduct(Product product) {
        if (product == null)
            return ServerResponse.createByError("新增或更新产品参数不正确");

        if (StringUtils.isNotBlank(product.getSubImages())) {
            String[] subImageArr = product.getSubImages().split(",");
            if (subImageArr.length > 0)
                product.setMainImage(subImageArr[0]);
        }
        if (product.getId() == null) {
            // 说明产品尚未被添加入数据库，则执行save操作
            int row_cnt = productMapper.insert(product);
            if (row_cnt > 0)
                return ServerResponse.createBySuccess("添加产品成功");
            return ServerResponse.createByError("添加产品失败");
        } else {
            // 产品id不为空，说明产品已经添加入数据库，则执行update操作
            // 这边使用updateByPrimaryKeySelective可能会更好些
            int row_cnt = productMapper.updateByPrimaryKey(product);
            if (row_cnt > 0) {
                return ServerResponse.createBySuccess("更新产品信息成功");
            }
            return ServerResponse.createByError("更新产品信息失败");
        }
    }

    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByError(
                    ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int row_cnt = productMapper.updateByPrimaryKeySelective(product);
        if (row_cnt > 0) {
            return ServerResponse.createBySuccess("修改产品销售状态成功");
        }
        return ServerResponse.createByError("修改产品销售状态失败");
    }

    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByError(
                    ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByError("产品已下架或删除");
        }
        ProductDetailVo productDetailVo = assembleProductVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImage(product.getSubImages());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setDetail(product.getDetail());

        productDetailVo.setImageHost(
                PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        productDetailVo.setParentCategoryId(category == null ? 0 : category.getParentId());
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        return productDetailVo;
    }

    /**
     * getProductList - 获取分页后的产品列表信息
     *
     * @param pageNum  : 第pageNum页
     * @param pageSize : 每页显示pageSize个商品
     * @return : 返回指定的pageNum页检索出的pageSize个商品信息
     * 初始化PageHelper ---> 检索mysql并把结果返回给productList
     * ---> 遍历productList，把每个产品需要的部分信息提取出并组成新的队列productListVoList
     * ---> 把productListVoList的信息装入PageInfo数据结构中并返回，这样pageHelper就可以起作用
     */
    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        // startPage -- start ---> 填充sql逻辑 ---> pageHelper -- 收尾
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product item : productList) {
            ProductListVo productListVo = assembleProductListVo(item);
            productListVoList.add(productListVo);
        }
        //PageInfo<?> pageResult = new PageInfo(productList);
        //pageResult.setList(productListVoList);
        PageInfo pageResult = new PageInfo(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    public ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        productListVo.setImageHost(
                PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        return productListVo;
    }

    public ServerResponse<PageInfo> searchProduct(
            String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            // 因为要使用sql语句的模糊查询功能，所以 productName 应该拼接成 %productName% 再传递给db
            productName = new StringBuilder().append("%").append(productName).append('%').toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product item : productList) {
            ProductListVo productListVo = assembleProductListVo(item);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        //PageInfo pageInfo = new PageInfo(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode()
                    , ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null || product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByError("产品已下架或删除");
        }
        ProductDetailVo productDetailVo = assembleProductVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword
            , Integer categoryId, int pageNum, int pageSize, String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode()
                    , ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();

        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                // 没有该分类，且没有关键字，这时候返回一个空集合，不报错
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        PageHelper.startPage(pageNum, pageSize);
        // 排序处理
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArr = orderBy.split("_");
                PageHelper.orderBy(orderByArr[0] + " " + orderByArr[1]);
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(
                StringUtils.isBlank(keyword) ? null : keyword
                , categoryIdList.size() == 0 ? null : categoryIdList
        );
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
