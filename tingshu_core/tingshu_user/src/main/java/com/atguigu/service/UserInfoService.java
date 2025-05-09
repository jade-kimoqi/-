package com.atguigu.service;

import com.atguigu.entity.UserInfo;
import com.atguigu.vo.UserPaidRecordVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户 服务类
 * </p>
 *
 * @author long
 * @since 2025-03-03
 */
public interface UserInfoService extends IService<UserInfo> {

    Map<Long, Boolean> getUserShowPaidMarkOrNot(Long albumId, List<Long> needPayTrackIdList);
    void updateUserPaidInfo(UserPaidRecordVo userPaidRecordVo);

    void changeVip(long userId);
}
