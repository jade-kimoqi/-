package com.atguigu.service.impl;

import com.atguigu.AlbumFeignClient;
import com.atguigu.constant.SystemConstant;
import com.atguigu.entity.*;
import com.atguigu.mapper.UserInfoMapper;
import com.atguigu.service.*;
import com.atguigu.util.AuthContextHolder;
import com.atguigu.vo.UserPaidRecordVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户 服务实现类
 * </p>
 *
 * @author long
 * @since 2025-03-03
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Autowired
    private UserPaidAlbumService userPaidAlbumService;
    @Autowired
    private UserPaidTrackService userPaidTrackService;

    @Override
    public Map<Long, Boolean> getUserShowPaidMarkOrNot(Long albumId, List<Long> needPayTrackIdList) {
        Map<Long, Boolean> showPaidMarkMap = new HashMap<>();
        Long userId = AuthContextHolder.getUserId();
        LambdaQueryWrapper<UserPaidAlbum> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPaidAlbum::getUserId,userId);
        wrapper.eq(UserPaidAlbum::getAlbumId,albumId);
        UserPaidAlbum userPaidAlbum = userPaidAlbumService.getOne(wrapper);
        if(userPaidAlbum!=null){
            //这个专辑里面的声音用户都不需要付费
            needPayTrackIdList.stream().forEach(trackId->{
                showPaidMarkMap.put(trackId,false);
            });
            return showPaidMarkMap;
        }else {
            //用户购买过的声音
            LambdaQueryWrapper<UserPaidTrack> paidTrackWrapper = new LambdaQueryWrapper<>();
            paidTrackWrapper.eq(UserPaidTrack::getUserId,userId);
            paidTrackWrapper.in(UserPaidTrack::getTrackId,needPayTrackIdList);
            
            //查询出已经支付过的声音列表
            List<UserPaidTrack> userPaidTrackList = userPaidTrackService.list(paidTrackWrapper);
            List<Long> paidTrackIdList = userPaidTrackList.stream().map(UserPaidTrack::getTrackId).collect(Collectors.toList());
            needPayTrackIdList.stream().forEach(trackId->{
                if(paidTrackIdList.contains(trackId)){
                    showPaidMarkMap.put(trackId,false);
                }else {
                    showPaidMarkMap.put(trackId,true);
                }
            });
        }
        return showPaidMarkMap;

    }
    @Autowired
    private AlbumFeignClient albumFeignClient;
    @Autowired
    private UserVipInfoService userVipInfoService;
    @Autowired
    private VipServiceConfigService vipServiceConfig;
    @Override
    public void updateUserPaidInfo(UserPaidRecordVo userPaidRecordVo) {
        //如果购买的是专辑
        if(userPaidRecordVo.getItemType().equals(SystemConstant.BUY_ALBUM)){
            long count = userPaidAlbumService.count(new LambdaQueryWrapper<UserPaidAlbum>().eq(UserPaidAlbum::getOrderNo, userPaidRecordVo.getOrderNo()));
            if(count>0) return;
            UserPaidAlbum userPaidAlbum = new UserPaidAlbum();
            userPaidAlbum.setUserId(userPaidRecordVo.getUserId());
            userPaidAlbum.setAlbumId(userPaidRecordVo.getItemIdList().get(0));
            userPaidAlbum.setOrderNo(userPaidRecordVo.getOrderNo());
            userPaidAlbumService.save(userPaidAlbum);
        }else if(userPaidRecordVo.getItemType().equals(SystemConstant.BUY_TRACK)){
            //如果购买的是声音
            long count = userPaidTrackService.count(new LambdaQueryWrapper<UserPaidTrack>().eq(UserPaidTrack::getOrderNo, userPaidRecordVo.getOrderNo()));
            if(count>0) return;
            TrackInfo trackInfo = albumFeignClient.getTrackInfoById(userPaidRecordVo.getItemIdList().get(0)).getData();
            List<UserPaidTrack> userPaidTrackList = userPaidRecordVo.getItemIdList().stream().map(trackId -> {
                UserPaidTrack userPaidTrack = new UserPaidTrack();
                userPaidTrack.setUserId(userPaidRecordVo.getUserId());
                userPaidTrack.setAlbumId(trackInfo.getAlbumId());
                userPaidTrack.setTrackId(trackId);
                userPaidTrack.setOrderNo(userPaidRecordVo.getOrderNo());
                return userPaidTrack;
            }).collect(Collectors.toList());
            userPaidTrackService.saveBatch(userPaidTrackList);
        }else if(userPaidRecordVo.getItemType().equals(SystemConstant.BUY_VIP)){
            //如果购买的是VIP
            Long vipConfigId = userPaidRecordVo.getItemIdList().get(0);
            VipServiceConfig vipConfig = vipServiceConfig.getById(vipConfigId);
            UserVipInfo userVipInfo = new UserVipInfo();
            userVipInfo.setUserId(userPaidRecordVo.getUserId());
            userVipInfo.setOrderNo(userPaidRecordVo.getOrderNo());
            //拿到用户信息
            UserInfo userInfo = getById(userPaidRecordVo.getUserId());
            Date startTime = new Date();
            //判断当前用户是否为vip 如果是vip并且没有过期 vip时间要累加
            if(userInfo.getIsVip()==1&&userInfo.getVipExpireTime().after(new Date())){
                startTime = userInfo.getVipExpireTime();
            }
            Date newExpireTime = new DateTime(startTime).plusMonths(vipConfig.getServiceMonth()).toDate();
            userVipInfo.setStartTime(startTime);
            userVipInfo.setExpireTime(newExpireTime);
            userVipInfoService.save(userVipInfo);
            //更新vip信息
            userInfo.setIsVip(1);
            userInfo.setVipExpireTime(newExpireTime);
            updateById(userInfo);
        }
    }

    @Override
    public void changeVip(long userId) {

        Date date = parseToDateJava8("2026-08-25 10:35:22");
        UserInfo userInfo = new UserInfo();
        userInfo.setIsVip(1);
        userInfo.setVipExpireTime(date);
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getId,userId);

        update(userInfo,wrapper);
    }

    public Date parseToDateJava8(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC)); // 转为 Date
    }
}
