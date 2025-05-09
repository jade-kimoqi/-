package com.atguigu.controller;

import com.atguigu.entity.BaseAttribute;
import com.atguigu.entity.BaseCategory1;
import com.atguigu.entity.BaseCategory3;
import com.atguigu.entity.BaseCategoryView;
import com.atguigu.login.TingShuLogin;
import com.atguigu.mapper.BaseAttributeMapper;
import com.atguigu.result.RetVal;
import com.atguigu.service.BaseCategory1Service;
import com.atguigu.service.BaseCategory3Service;
import com.atguigu.service.BaseCategoryViewService;
import com.atguigu.vo.CategoryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 一级分类表 前端控制器
 * </p>
 *
 * @author long
 * @since 2025-03-07
 */
@Tag(name = "分类管理")
@RestController
@RequestMapping(value = "api/album/category")
public class CategoryController {
    //    http://127.0.0.1/api/album/category/getAllCategoryList
    @Autowired
    private BaseCategoryViewService categoryViewService;
    @Autowired
    private BaseAttributeMapper propertyKeyMapper;
    @GetMapping("getAllCategoryList")
    @Operation(summary = "获取全部分类信息")
    @TingShuLogin
    public RetVal getAllCategoryList(){
        List<CategoryVo> categoryVoList=categoryViewService.getAllCategoryList(null);
        return RetVal.ok(categoryVoList);

    }
    @GetMapping("/getPropertyByCategory1Id/{category1Id}")
    @Operation(summary = "根据一级分类id，查询分类属性信息")
    public RetVal getPropertyByCategory1Id(@PathVariable long category1Id){

        List<BaseAttribute> categoryPropertyList=propertyKeyMapper.getPropertyByCategory1Id(category1Id);
        return RetVal.ok(categoryPropertyList);
    }

//
//    下面是搜索模块
    @Autowired
    private BaseCategory3Service baseCategory3Service;

    @GetMapping("getCategoryView/{category3Id}")
    @Operation(summary = "通过三级分类id查询分类信息")
    public BaseCategoryView getCategoryView(@PathVariable long category3Id){
        BaseCategoryView categoryView = categoryViewService.getById(category3Id);
        return categoryView;

    }
//    http://127.0.0.1/api/album/category/getCategory3ListByCategory1Id/1
    @Operation(summary = "根据一级分类id获取三级分类列表")
    @GetMapping("getCategory3ListByCategory1Id/{category1Id}")
    public RetVal<List<BaseCategory3>> getCategory3ListByCategory1Id(@PathVariable Long category1Id) {
        List<BaseCategory3> category3List=  baseCategory3Service.getCategory3ListByCategory1Id(category1Id);
        return RetVal.ok(category3List);
    }
    @Operation(summary = "根据一级分类id获取全部分类信息")
    @GetMapping("getCategoryByCategory1Id/{category1Id}")
    public RetVal getCategoryByCategory1Id(@PathVariable Long category1Id) {
        List<CategoryVo> allCategoryList=  categoryViewService.getAllCategoryList(category1Id);
        if(!CollectionUtils.isEmpty(allCategoryList)){
            return RetVal.ok(allCategoryList.get(0));
        }
        return RetVal.ok();
    }

    @Autowired
    private BaseCategory1Service category1Service;

    @Operation(summary = "查询所有一级分类")
    @GetMapping("getCategory1")
    public List<BaseCategory1> getCategory1() {
        LambdaQueryWrapper<BaseCategory1> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BaseCategory1::getOrderNum);
        return category1Service.list(wrapper);
    }



}
