package com.atguigu.consumer;

import com.alibaba.fastjson.JSON;
import com.atguigu.constant.KafkaConstant;
import com.atguigu.service.UserInfoService;
import com.atguigu.vo.UserPaidRecordVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserConsumer {
    @Autowired
    private UserInfoService userInfoService;

    @KafkaListener(topics = KafkaConstant.USER_PAID_QUEUE)
    public void updateUserPaidRecord(String data){
        UserPaidRecordVo userPaidRecordVo = JSON.parseObject(data, UserPaidRecordVo.class);
        //更新用户支付信息
        userInfoService.updateUserPaidInfo(userPaidRecordVo);

    }
}