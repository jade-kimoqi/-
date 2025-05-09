package com.atguigu.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.constant.SystemConstant;
import com.atguigu.entity.RechargeInfo;
import com.atguigu.entity.UserAccount;
import com.atguigu.entity.UserAccountDetail;
import com.atguigu.execption.GuiguException;
import com.atguigu.mapper.UserAccountMapper;
import com.atguigu.result.ResultCodeEnum;
import com.atguigu.result.RetVal;
import com.atguigu.service.RechargeInfoService;
import com.atguigu.service.UserAccountDetailService;
import com.atguigu.service.UserAccountService;
import com.atguigu.util.AuthContextHolder;
import com.atguigu.vo.AccountLockResultVo;
import com.atguigu.vo.AccountLockVo;
import com.atguigu.vo.RechargeInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author long
 * @since 2025-04-18
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserAccountDetailService accountDetailService;
    @Transactional
    @Override
    public RetVal checkAndLock(AccountLockVo accountLockVo) {
        String checkAndLockKey="check:lock:"+accountLockVo.getOrderNo();
        String lockAccountKey="lock:account:"+accountLockVo.getOrderNo();
        //防止重复提交
        Boolean isExist = redisTemplate.opsForValue().setIfAbsent(checkAndLockKey, accountLockVo.getOrderNo(), 20, TimeUnit.SECONDS);
        if(!isExist){
            //存在重复提交
            String data = redisTemplate.opsForValue().get(lockAccountKey);
            if(!StringUtils.isEmpty(data)){
                AccountLockResultVo accountLockResultVo = JSONObject.parseObject(data, AccountLockResultVo.class);
                return RetVal.ok(accountLockResultVo);
            }
            return RetVal.build(null, ResultCodeEnum.ACCOUNT_LOCK_REPEAT);
        }
        UserAccount userAccount=baseMapper.checkAndLock(accountLockVo.getUserId(),accountLockVo.getAmount());
        if(userAccount==null){
            redisTemplate.delete(checkAndLockKey);
            return RetVal.build(null, ResultCodeEnum.ACCOUNT_BALANCES_NOT_ENOUGH);
        }
        //如果账号余额足够 锁定账号余额
        int rows=baseMapper.lockUserAccount(accountLockVo.getUserId(),accountLockVo.getAmount());
        if(rows==0){
            redisTemplate.delete(checkAndLockKey);
            return RetVal.build(null, ResultCodeEnum.ACCOUNT_LOCK_ERROR);
        }
        //添加账号明细
        UserAccountDetail userAccountDetail = new UserAccountDetail();
        userAccountDetail.setUserId(accountLockVo.getUserId());
        userAccountDetail.setTitle("锁定："+accountLockVo.getContent());
        userAccountDetail.setTradeType(SystemConstant.ACCOUNT_TRADE_TYPE_LOCK);
        userAccountDetail.setAmount(accountLockVo.getAmount());
        userAccountDetail.setOrderNo("lock:"+accountLockVo.getOrderNo());
        accountDetailService.save(userAccountDetail);
        //返回锁定对象
        AccountLockResultVo accountLockResultVo = new AccountLockResultVo();
        accountLockResultVo.setUserId(accountLockVo.getUserId());
        accountLockResultVo.setAmount(accountLockVo.getAmount());
        accountLockResultVo.setContent(accountLockVo.getContent());
        //锁定信息放到redis里面
        redisTemplate.opsForValue().set(lockAccountKey, JSON.toJSONString(accountLockResultVo),30,TimeUnit.SECONDS);
        return  RetVal.ok(accountLockResultVo);
    }

    @Override
    public void deductLockAccount(String orderNo) {
        String deductAccountKey="deduct:account:"+orderNo;
        String lockAccountKey="lock:account:"+orderNo;
        //防止重复提交
        boolean isExist = redisTemplate.opsForValue().setIfAbsent(deductAccountKey, orderNo, 20, TimeUnit.SECONDS);
        if(!isExist) return;

        String data = redisTemplate.opsForValue().get(lockAccountKey);
        if(!StringUtils.isEmpty(data)){
            AccountLockResultVo lockVo = JSONObject.parseObject(data, AccountLockResultVo.class);
            //解除锁定金额
            int rows= baseMapper.deductLockAccount(lockVo.getUserId(),lockVo.getAmount());
            if(rows==0){
                redisTemplate.delete(deductAccountKey);
                throw new GuiguException(ResultCodeEnum.ACCOUNT_MINUSLOCK_ERROR);
            }
            //添加账号明细
            UserAccountDetail  userAccountDetail= new UserAccountDetail();
            userAccountDetail.setUserId(lockVo.getUserId());
            userAccountDetail.setTitle("扣减："+lockVo.getContent());
            userAccountDetail.setTradeType(SystemConstant.ACCOUNT_EXPENSE);
            userAccountDetail.setAmount(lockVo.getAmount());
            userAccountDetail.setOrderNo(orderNo);
            accountDetailService.save(userAccountDetail);
            redisTemplate.delete(deductAccountKey);
        }
    }

    @Override
    public void unLockAccount(String orderNo) {
        String unLockKey="unlock"+orderNo;
        String lockAccountKey="lock:account:"+orderNo;
        //防止重复提交
        boolean isExist = redisTemplate.opsForValue().setIfAbsent(unLockKey, orderNo, 20, TimeUnit.SECONDS);
        if(!isExist) return;
        String data = redisTemplate.opsForValue().get(lockAccountKey);
        if(!StringUtils.isEmpty(data)){
            AccountLockResultVo lockVo = JSONObject.parseObject(data, AccountLockResultVo.class);
            //解除锁定
            int rows= baseMapper.unLockUserAccount(lockVo.getUserId(),lockVo.getAmount());
            if(rows==0){
                redisTemplate.delete(unLockKey);
                throw new GuiguException(ResultCodeEnum.ACCOUNT_UNLOCK_ERROR);
            }
            //添加账号明细
            UserAccountDetail  userAccountDetail= new UserAccountDetail();
            userAccountDetail.setUserId(lockVo.getUserId());
            userAccountDetail.setTitle("解锁："+lockVo.getContent());
            userAccountDetail.setTradeType(SystemConstant.UNLOCK_ACCOUNT);
            userAccountDetail.setAmount(lockVo.getAmount());
            userAccountDetail.setOrderNo("unlock:"+orderNo);
            accountDetailService.save(userAccountDetail);
            redisTemplate.delete(lockAccountKey);
        }

    }
@Autowired
private RechargeInfoService rechargeInfoService;
    @Override
    public void saveRecharge(RechargeInfoVo rechargeInfoVo) {
        Long userId = AuthContextHolder.getUserId();
        String key = UUID.randomUUID().toString().replaceAll("-","");
        BigDecimal rechargeAmount = rechargeInfoVo.getAmount();
        RechargeInfo rechargeInfo = new RechargeInfo();
        BeanUtils.copyProperties(rechargeInfoVo,rechargeInfo);
        rechargeInfo.setUserId(userId);
        rechargeInfo.setRechargeAmount(rechargeAmount);
        rechargeInfo.setOrderNo(key);
        LambdaQueryWrapper<RechargeInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeInfo::getUserId,userId);
        rechargeInfoService.update(rechargeInfo,wrapper);
        UserAccountDetail userAccountDetail = new UserAccountDetail();
        BeanUtils.copyProperties(rechargeInfo,userAccountDetail);
        userAccountDetail.setAmount(rechargeAmount);
        userAccountDetail.setTradeType("1201");
        userAccountDetail.setOrderNo(rechargeInfo.getOrderNo());
        userAccountDetail.setTitle("充值：");


        userAccountDetailService.save(userAccountDetail);
        baseMapper.addAmount(userId,rechargeAmount);

    }
    @Autowired
    private UserAccountDetailService userAccountDetailService;

    @Override
    public void getUserAccountDetailByPage(IPage<UserAccountDetail> pageParam, String tradeType) {
        Long userId = AuthContextHolder.getUserId();
        //交易类型：1201-充值 1202-锁定 1203-解锁 1204-消费
        LambdaQueryWrapper<UserAccountDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccountDetail::getUserId,userId);
        wrapper.eq(UserAccountDetail::getTradeType,tradeType);
        userAccountDetailService.page(pageParam,wrapper);

    }

//    @Override
//    public void getRecordByPage(IPage<RechargeInfo> paramPage, String tradeType) {
//        Long userId = AuthContextHolder.getUserId();
//        LambdaQueryWrapper<RechargeInfo> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(RechargeInfo::getUserId,userId);
//        rechargeInfoService.page(paramPage,wrapper);
//    }
}
