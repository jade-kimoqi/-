package com.atguigu.mapper;

import com.atguigu.entity.UserAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 用户账户 Mapper 接口
 * </p>
 *
 * @author long
 * @since 2025-04-18
 */
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    UserAccount checkAndLock(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    int lockUserAccount(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    int deductLockAccount(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    int unLockUserAccount(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    void addAmount(@Param("userId") Long userId, @Param("rechargeAmount") BigDecimal rechargeAmount);
}
