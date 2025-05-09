package com.atguigu.controller;

import com.atguigu.entity.UserAccountDetail;
import com.atguigu.login.TingShuLogin;
import com.atguigu.result.RetVal;
import com.atguigu.service.RechargeInfoService;
import com.atguigu.service.UserAccountService;
import com.atguigu.vo.RechargeInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 充值信息 前端控制器
 * </p>
 *
 * @author long
 * @since 2025-04-18
 */
@Tag(name = "充值管理")
@RestController
@RequestMapping("/api/account/recharge")
public class RechargeController {
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private RechargeInfoService rechargeInfoService;
//    http://127.0.0.1/api/account/recharge/saveRecharge
    @Operation(summary = "充钱")
    @TingShuLogin
    @PostMapping("saveRecharge")
    public RetVal saveRecharge(@RequestBody RechargeInfoVo rechargeInfoVo){
        userAccountService.saveRecharge(rechargeInfoVo);
        return RetVal.ok();
    }
//    http://127.0.0.1/api/account/recharge/getRecordByPage/1/10/1201
@Operation(summary = "充钱记录")
@TingShuLogin
@GetMapping("getRecordByPage/{pageNum}/{pageSize}/{tradeType}")
public RetVal getRecordByPage(
        @PathVariable Long pageNum,
        @PathVariable Long pageSize,
        @PathVariable String tradeType) {
    IPage<UserAccountDetail> pageParam = new Page<>(pageNum, pageSize);
    //"交易类型：1201-充值 1202-锁定 1203-解锁 1204-消费"
    userAccountService.getUserAccountDetailByPage(pageParam,tradeType);
    return RetVal.ok(pageParam);
}


}
