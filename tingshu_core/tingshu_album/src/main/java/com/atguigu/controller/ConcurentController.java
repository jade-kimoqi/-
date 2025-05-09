package com.atguigu.controller;

import com.atguigu.entity.AlbumInfo;
import com.atguigu.service.AlbumInfoService;
import com.atguigu.service.BaseCategory1Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "并发管理接口")
@RestController
@RequestMapping("/api/album")
public class ConcurentController {
@Autowired
private BaseCategory1Service baseCategory1Service;
@Autowired
private AlbumInfoService albumInfoService;
@Autowired
private RBloomFilter albumBloomFilter;


    @Operation(summary = "例子")
    @GetMapping("setNum")
    public String setNum() throws Exception {
    baseCategory1Service.setNum();
        return "success";

    }

    @Operation(summary = "初始化布隆过滤器")
    @GetMapping("init")
    public String init(){
        LambdaQueryWrapper<AlbumInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(AlbumInfo::getId);
        List<AlbumInfo> albumInfoList = albumInfoService.list(wrapper);
        for (AlbumInfo albumInfo : albumInfoList) {
            Long id = albumInfo.getId();
            albumBloomFilter.add(id);
        }

        return "success";
    }

}
