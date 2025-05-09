package com.atguigu.consumer;

import com.atguigu.constant.KafkaConstant;
import com.atguigu.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AccountConsumer {
    @Autowired
    private UserAccountService accountService;
    //扣减锁定金额
    @KafkaListener(topics = KafkaConstant.DEDUCT_LOCK_ACCOUNT_QUEUE)
    public void deductLockAccount(String orderNo) {
        if(!StringUtils.isEmpty(orderNo)){
            accountService.deductLockAccount(orderNo);
        }
    }
    //解锁锁定金额与账户锁
    @KafkaListener(topics = KafkaConstant.UNLOCK_ACCOUNT_QUEUE)
    public void unLockAccount(String orderNo) {
        if(!StringUtils.isEmpty(orderNo)){
            accountService.unLockAccount(orderNo);
        }
    }
}
