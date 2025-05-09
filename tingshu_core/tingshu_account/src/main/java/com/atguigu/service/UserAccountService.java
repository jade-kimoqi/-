package com.atguigu.service;

import com.atguigu.entity.UserAccount;
import com.atguigu.entity.UserAccountDetail;
import com.atguigu.result.RetVal;
import com.atguigu.vo.AccountLockVo;
import com.atguigu.vo.RechargeInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户账户 服务类
 * </p>
 *
 * @author long
 * @since 2025-04-18
 */
public interface UserAccountService extends IService<UserAccount> {

    RetVal checkAndLock(AccountLockVo accountLockVo);

    void deductLockAccount(String orderNo);

    void unLockAccount(String orderNo);

    void saveRecharge(RechargeInfoVo rechargeInfoVo);

    void getUserAccountDetailByPage(IPage<UserAccountDetail> pageParam, String tradeType);

//    void getRecordByPage(IPage<RechargeInfo> paramPage, String tradeType);
}
