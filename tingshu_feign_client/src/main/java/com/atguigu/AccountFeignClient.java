package com.atguigu;

import com.atguigu.result.RetVal;
import com.atguigu.vo.AccountLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "tingshu-account")
public interface AccountFeignClient {
    @PostMapping("/api/account/userAccount/checkAndLock")
    public RetVal checkAndLock(@RequestBody AccountLockVo accountLockVo);
}
