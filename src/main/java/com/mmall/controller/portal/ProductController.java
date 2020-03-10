package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;

    // 不使用RESTful风格的URI: www.imooc11.com/product/detail.do?productId=26
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        return iProductService.getProductDetail(productId);
    }

    // 使用RESTful风格的URI： www.imooc11.com/product/26
    // 注：不是所有URI都适合改造成RESTful风格的,如显示数据内容比较适合RESTful,添加就不适合
    @RequestMapping(value = "/{productId}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> detailRESTful(@PathVariable Integer productId) {
        return iProductService.getProductDetail(productId);
    }

    // @keyword: 关键字模糊查询
    // @categoryId: 品类ID，即在该品类下查询 keyword
    // @orderBy: 按价格 升/降序 排序 - 由PageHelper插件完成
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(
            @RequestParam(value = "keyword", required = false) String keyword
            , @RequestParam(value = "categoryId", required = false) Integer categoryId
            , @RequestParam(value = "pageNum", defaultValue = "1") int pageNum
            , @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
            , @RequestParam(value = "orderBy", defaultValue = "") String orderBy) {
        return iProductService.getProductByKeywordCategory(keyword, categoryId, pageNum, pageSize, orderBy);
    }

    // list.do需要三种情况的RESTful URL 判断： 1. keyword 和 category 都不为空的；2. keyword 为空的；3. categoryId 为空的
    @RequestMapping(value = "/{keyword}/{categoryId}/{pageNum}/{pageSize}/{orderBy}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> listRESTful(@PathVariable(value = "keyword") String keyword
            , @PathVariable(value = "categoryId") Integer categoryId
            , @PathVariable(value = "pageNum") Integer pageNum
            , @PathVariable(value = "pageSize") Integer pageSize
            , @PathVariable(value = "orderBy") String orderBy) {

        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        if (StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }
        return iProductService.getProductByKeywordCategory(keyword, categoryId, pageNum, pageSize, orderBy);
    }

    // http://www.imooc11.com/product/keyword/手机/1/10/price_asc
    @RequestMapping(value = "/keyword/{keyword}/{pageNum}/{pageSize}/{orderBy}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> listRESTful(@PathVariable(value = "keyword") String keyword
            , @PathVariable(value = "pageNum") Integer pageNum
            , @PathVariable(value = "pageSize") Integer pageSize
            , @PathVariable(value = "orderBy") String orderBy) {
        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        if (StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }
        return iProductService.getProductByKeywordCategory(keyword, null, pageNum, pageSize, orderBy);
    }

    // http://www.imooc11.com/product/category/100012/1/10/price_asc
    @RequestMapping(value = "/category/{categoryId}/{pageNum}/{pageSize}/{orderBy}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> listRESTful(@PathVariable(value = "categoryId") Integer categoryId
            , @PathVariable(value = "pageNum") Integer pageNum
            , @PathVariable(value = "pageSize") Integer pageSize
            , @PathVariable(value = "orderBy") String orderBy) {

        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        if (StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }
        return iProductService.getProductByKeywordCategory("", categoryId, pageNum, pageSize, orderBy);
    }

    // 这是错误的 RESTful 风格的URL，因为如果keyword为空则URI定位会出现问题
//    @RequestMapping(value = "/{keyword}/{pageNum}/{pageSize}/{orderBy}",method = RequestMethod.GET)
//    @ResponseBody
//    public ServerResponse<PageInfo> listRESTfulBadcase(@PathVariable(value = "keyword")String keyword,
//                                                       @PathVariable(value = "pageNum") Integer pageNum,
//                                                       @PathVariable(value = "pageSize") Integer pageSize,
//                                                       @PathVariable(value = "orderBy") String orderBy){
//        if(pageNum == null){
//            pageNum = 1;
//        }
//        if(pageSize == null){
//            pageSize = 10;
//        }
//        if(StringUtils.isBlank(orderBy)){
//            orderBy = "price_asc";
//        }
//
//        return iProductService.getProductByKeywordCategory(keyword,null,pageNum,pageSize,orderBy);
//    }
}
