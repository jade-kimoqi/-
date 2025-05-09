package com.atguigu.controller;

import com.atguigu.entity.AlbumAttributeValue;
import com.atguigu.entity.AlbumInfo;
import com.atguigu.login.TingShuLogin;
import com.atguigu.mapper.AlbumInfoMapper;
import com.atguigu.mapper.AlbumStatMapper;
import com.atguigu.query.AlbumInfoQuery;
import com.atguigu.result.RetVal;
import com.atguigu.service.AlbumAttributeValueService;
import com.atguigu.service.AlbumInfoService;
import com.atguigu.service.ListenService;
import com.atguigu.util.AuthContextHolder;
import com.atguigu.vo.AlbumStatVo;
import com.atguigu.vo.AlbumTempVo;
import com.atguigu.vo.UserCollectVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="专辑管理")
@RestController
@RequestMapping("/api/album/albumInfo")
public class AlbumController {
//    Request URL: http://127.0.0.1/api/album/albumInfo/saveAlbumInfo
    @Autowired
    private AlbumInfoService albumInfoService;
    @Autowired
    private AlbumInfoMapper albumInfoMapper;


    @Operation(summary = "新增专辑")
    @PostMapping("saveAlbumInfo")
    @TingShuLogin
    public RetVal<AlbumInfo> getSaveAlbumInfo(@RequestBody AlbumInfo albumInfo){
        albumInfoService.saveAlbumInfo(albumInfo);
        return RetVal.ok();

    }
//    http://127.0.0.1/api/album/albumInfo/getUserSubscribeByPage/1/10
    @PostMapping("getUserAlbumByPage/{pageNum}/{pageSize}")
    @Operation(summary = "分页查询所有专辑")
   @TingShuLogin
    public RetVal getUserAlbumByPage(
            @Parameter(name = "pageNum",description = "当前页码",required = true)
            @PathVariable Long pageNum,
            @Parameter(name = "pageNum",description = "页数",required = true)
            @PathVariable Long pageSize,
            @Parameter(name = "albumInfoQuery",description = "查询对象",required = false)
            @RequestBody AlbumInfoQuery albumInfoQuery){
       Long userId =  AuthContextHolder.getUserId();
       albumInfoQuery.setUserId(userId);

       IPage<AlbumTempVo> pageParam = new Page<>(pageNum,pageSize);
        pageParam=albumInfoMapper.getUserAlbumByPage(pageParam,albumInfoQuery);
        return RetVal.ok(pageParam);
    }
//    http://127.0.0.1/api/album/albumInfo/getAlbumInfoById/1600
@GetMapping("getAlbumInfoById/{albumId}")
    @Operation(summary = "根据ID查询分类信息")
    public RetVal getAlbumInfoById(@PathVariable Long albumId){
        AlbumInfo albumInfo = albumInfoService.getAlbumInfoById(albumId);
        return RetVal.ok(albumInfo);
}
//    http://127.0.0.1/api/album/albumInfo/updateAlbumInfo
    @Operation(summary = "修改专辑")
    @PutMapping("updateAlbumInfo")
    public RetVal updateAlbumInfo(@RequestBody AlbumInfo albumInfo){
        albumInfoService.updateAlbumInfo(albumInfo);
        return RetVal.ok();
    }
//    http://127.0.0.1/api/album/albumInfo/deleteAlbumInfo/1600
    @Operation(summary = "删除专辑")
    @DeleteMapping("deleteAlbumInfo/{albumId}")
    public RetVal deleteAlbumInfo(@PathVariable Long albumId){
        albumInfoService.deleteAlbumInfo(albumId);
        return RetVal.ok();
    }
//
//  以下是搜索模块内容
    @Autowired
    private AlbumAttributeValueService albumAttributeValueService;
    @Operation(summary = "专辑属性值列表")
    @GetMapping("getAlbumPropertyValue/{albumId}")
    public List<AlbumAttributeValue> getAlbumPropertyValue(@PathVariable Long albumId){
        LambdaQueryWrapper<AlbumAttributeValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlbumAttributeValue::getAlbumId,albumId);
        List<AlbumAttributeValue> attributeValueList = albumAttributeValueService.list(wrapper);
        return attributeValueList;
    }
//下面是专辑详情内容
@Autowired
private AlbumStatMapper albumStatMapper;

    @Operation(summary = "获取专辑统计信息")
    @GetMapping("getAlbumStatInfo/{albumId}")
    public AlbumStatVo getAlbumStatInfo(@PathVariable Long albumId) {
      AlbumStatVo albumStatVo =  albumStatMapper.getAlbumStatInfo(albumId);
      return albumStatVo;
    }
    //http://127.0.0.1/api/album/albumInfo/isSubscribe/139
    @TingShuLogin
    @Operation(summary = "是否订阅")
    @GetMapping("isSubscribe/{albumId}")
    public RetVal isSubscribe(@PathVariable Long albumId) {
       boolean flag = albumInfoService.isSubscribe(albumId);
       return RetVal.ok(flag);
    }
//    http://127.0.0.1/api/album/albumInfo/getUserSubscribeByPage/1/10
    @Autowired
    private ListenService listenService;
@TingShuLogin
@Operation(summary = "声音的统计列表")
@GetMapping("getUserSubscribeByPage/{pageNum}/{pageSize}")
public RetVal getUserCollectByPage(@PathVariable Integer pageNum,
                                   @PathVariable Integer pageSize) {
    IPage<UserCollectVo> userCollectByPage = listenService.getUserCollectByPage(pageNum, pageSize);

    return RetVal.ok(userCollectByPage);
    }
}
