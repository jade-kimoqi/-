package com.atguigu.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.atguigu.constant.RedisConstant;
import com.atguigu.entity.UserInfo;
import com.atguigu.login.TingShuLogin;
import com.atguigu.result.RetVal;
import com.atguigu.service.UserInfoService;
import com.atguigu.util.AuthContextHolder;
import com.atguigu.vo.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * vip服务配置表 前端控制器
 * </p>
 *
 * @author long
 * @since 2025-03-03
 */
@RestController
@RequestMapping("/api/user/wxLogin")
public class WxLoginController {
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Operation(summary="小程序授权登录")
    @GetMapping("wxLogin/{code}")
    public RetVal wxlogin(@PathVariable String code) throws Exception {
        //get oppenId
        WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
        String openid = sessionInfo.getOpenid();
        //SELECT * FROM `user_info`where wx_open_id='odo3j4qjcVhjHxPX4A4bmmyVJ4O0'
        //查询数据库是否有用户信息
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getWxOpenId,openid);
        UserInfo userInfo = userInfoService.getOne(wrapper);

        //如果不存在,就添加
        if(userInfo==null){
            userInfo=new UserInfo();
            userInfo.setNickname("听友"+System.currentTimeMillis());
            userInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            userInfo.setWxOpenId(openid);
            //isVip
            userInfo.setIsVip(0);
            userInfoService.save(userInfo);
        }
        String uuid = UUID.randomUUID().toString().replace("-","");
        String userKey=RedisConstant.USER_LOGIN_KEY_PREFIX+uuid;
        redisTemplate.opsForValue().set(userKey,userInfo,RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);
        HashMap<String, Object> retMap = new HashMap<>();
        retMap.put("token",uuid);
        return RetVal.ok(retMap);


    }
    @TingShuLogin
    @Operation(summary = "更新用户信息")
    @GetMapping("getUserInfo")
    public RetVal getUserInfo(){
        Long userId = AuthContextHolder.getUserId();
        UserInfo userInfo = userInfoService.getById((userId));
        UserInfoVo userInfoVo=new UserInfoVo();
        BeanUtils.copyProperties(userInfo,userInfoVo);

        return RetVal.ok(userInfoVo);
    }
}
