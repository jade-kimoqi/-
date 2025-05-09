package com.atguigu.controller;

import com.atguigu.login.TingShuLogin;
import com.atguigu.mapper.TrackInfoMapper;
import com.atguigu.result.RetVal;
import com.atguigu.service.ListenService;
import com.atguigu.vo.TrackStatVo;
import com.atguigu.vo.UserCollectVo;
import com.atguigu.vo.UserListenProcessVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "听专辑管理接口")
@RestController
@RequestMapping(value = "/api/album/progress")
public class ListenController {
    @Autowired
    private ListenService listenService;
    @Autowired
    private TrackInfoMapper trackInfoMapper;



    @TingShuLogin
    @Operation(summary = "更新播放进度")
    @PostMapping("updatePlaySecond")
    public RetVal updatePlaySecond(@RequestBody UserListenProcessVo userListenProcessVo) {
        listenService.updatePlaySecond(userListenProcessVo);
        return RetVal.ok();
    }
    @TingShuLogin
    @Operation(summary = "最近播放")
    @GetMapping("getRecentlyPlay")
    public RetVal getRecentlyPlay() {
        Map<String, Object> retMap = listenService.getRecentlyPlay();
        return RetVal.ok(retMap);
    }
//    http://127.0.0.1/api/album/progress/getTrackStatistics/7217
    @Operation(summary = "获取声音统计信息")
    @GetMapping("getTrackStatistics/{trackId}")
    public RetVal getTrackStatistics(@PathVariable Long trackId){
        TrackStatVo trackStatVo=trackInfoMapper.getTrackStatistics(trackId);
        return RetVal.ok(trackStatVo);
    }
    @TingShuLogin
    @Operation(summary = "收藏声音")
    @GetMapping("collectTrack/{trackId}")
    public RetVal collectTrack(@PathVariable Long trackId) {
        boolean flag = listenService.collectTrack(trackId);
        return RetVal.ok(flag);
    }
//    http://127.0.0.1/api/album/progress/getLastPlaySecond/7217
    @TingShuLogin
@Operation(summary = "最近播放")
@GetMapping("getLastPlaySecond/{trackId}")
public RetVal getLastPlaySecond(@PathVariable Long trackId){
      BigDecimal second= listenService.getLastPlaySecond(trackId);
    return RetVal.ok(second);
}
//    http://127.0.0.1/api/album/progress/isCollect/7217
    @TingShuLogin
@Operation(summary = "是否收藏声音")
@GetMapping("isCollect/{trackId}")
public RetVal isCollect(@PathVariable Long trackId){
    boolean flag=listenService.isCollect(trackId);
    return RetVal.ok(flag);
}
    @TingShuLogin
    @Operation(summary = "声音的统计列表")
    @GetMapping("getUserCollectByPage/{pageNum}/{pageSize}")
    public RetVal getUserCollectByPage(@PathVariable Integer pageNum,
                                       @PathVariable Integer pageSize)  {
        IPage<UserCollectVo> pageParam = listenService.getUserCollectByPage(pageNum,pageSize);
        return RetVal.ok(pageParam);
    }
    @TingShuLogin
    @Operation(summary = "获取用户声音历史播放列表")
    @GetMapping("getPlayHistoryTrackByPage/{pageNum}/{pageSize}")
    public RetVal getPlayHistoryTrackByPage(@PathVariable Integer pageNum,
                                            @PathVariable Integer pageSize)  {
//        IPage pageParam=listenService.getPlayHistoryTrackByPage(pageNum,pageSize);
        IPage<UserCollectVo> pageParam = listenService.getUserCollectByPage(pageNum,pageSize);
        return RetVal.ok(pageParam);
    }


//    http://127.0.0.1/api/comment/findCommentPage/139/7217/1/30
//    @Operation(summary = "")
//    @GetMapping("findCommentPage/{trackId}")
//    public RetVal findCommentPage(@PathVariable Long trackId){
//        return null;
//    }
}