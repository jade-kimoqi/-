package com.atguigu.service;

import com.atguigu.entity.BaseCategoryView;
import com.atguigu.vo.CategoryVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * VIEW 服务类
 * </p>
 *
 * @author long
 * @since 2025-03-07
 */
public interface BaseCategoryViewService extends IService<BaseCategoryView> {



    List<CategoryVo> getAllCategoryList(Long category1Id);
}
