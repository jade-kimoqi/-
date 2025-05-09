package com.atguigu.controller;

import com.atguigu.entity.UserAccount;
import com.atguigu.login.TingShuLogin;
import com.atguigu.result.RetVal;
import com.atguigu.service.UserAccountService;
import com.atguigu.util.AuthContextHolder;
import com.atguigu.vo.AccountLockVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户账户 前端控制器
 * </p>
 *
 * @author 强哥
 * @since 2023-12-16
 */
@Tag(name = "账户管理")
@RestController
@RequestMapping(value = "/api/account/userAccount")
public class AccountController {
    @Autowired
    private UserAccountService userAccountService;
    @TingShuLogin
    @Operation(summary = "获取账户可用金额")
    @GetMapping("getAvailableAmount")
    public RetVal getAvailableAmount()  {
        Long userId = AuthContextHolder.getUserId();
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getUserId,userId);
        UserAccount userAccount = userAccountService.getOne(wrapper);
        return RetVal.ok(userAccount.getAvailableAmount());
    }
    @TingShuLogin
    @Operation(summary = "锁定账号金额")
    @PostMapping("checkAndLock")
    public RetVal checkAndLock(@RequestBody AccountLockVo accountLockVo)  {
        return userAccountService.checkAndLock(accountLockVo);
    }
}
