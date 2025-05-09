package com.atguigu.service.impl;

import com.atguigu.entity.BaseCategory2;
import com.atguigu.entity.BaseCategory3;
import com.atguigu.mapper.BaseCategory3Mapper;
import com.atguigu.service.BaseCategory2Service;
import com.atguigu.service.BaseCategory3Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 三级分类表 服务实现类
 * </p>
 *
 * @author long
 * @since 2025-03-07
 */
@Service
public class BaseCategory3ServiceImpl extends ServiceImpl<BaseCategory3Mapper, BaseCategory3> implements BaseCategory3Service {
        @Autowired
        private BaseCategory2Service baseCategory2Service;
        @Autowired
        private BaseCategory3Service baseCategory3Service;
    @Override
    public List<BaseCategory3> getCategory3ListByCategory1Id(Long category1Id) {
        //根据一级分类id查询二级分类信息
        LambdaQueryWrapper<BaseCategory2> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(BaseCategory2::getCategory1Id,category1Id);
        wrapper2.select(BaseCategory2::getId);
        wrapper2.orderByDesc(BaseCategory2::getOrderNum);
        List<BaseCategory2> category2List = baseCategory2Service.list(wrapper2);

//        根据二级分类id查找三级分类信息
        List<Long> collect = category2List.stream().map(BaseCategory2::getId).collect(Collectors.toList());
        LambdaQueryWrapper<BaseCategory3> wrapper3 = new LambdaQueryWrapper<>();
        wrapper3.in(BaseCategory3::getCategory2Id,collect);
        wrapper3.eq(BaseCategory3::getIsTop,1).last("limit 7");
        return baseCategory3Service.list(wrapper3);
    }
}
