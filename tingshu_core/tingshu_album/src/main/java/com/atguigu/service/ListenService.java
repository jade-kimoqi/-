package com.atguigu.service;

import com.atguigu.vo.UserCollectVo;
import com.atguigu.vo.UserListenProcessVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;
import java.util.Map;

public interface ListenService {

    void updatePlaySecond(UserListenProcessVo userListenProcessVo);

    Map<String, Object> getRecentlyPlay();

    BigDecimal getLastPlaySecond(Long trackId);

    boolean collectTrack(Long trackId);

    boolean isCollect(Long trackId);

    IPage<UserCollectVo> getUserCollectByPage(Integer pageNum, Integer pageSize);

    IPage getPlayHistoryTrackByPage(Integer pageNum, Integer pageSize);
}