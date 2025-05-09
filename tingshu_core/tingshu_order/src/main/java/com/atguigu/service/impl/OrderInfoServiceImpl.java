package com.atguigu.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.AccountFeignClient;
import com.atguigu.AlbumFeignClient;
import com.atguigu.UserFeignClient;
import com.atguigu.constant.KafkaConstant;
import com.atguigu.constant.SystemConstant;
import com.atguigu.entity.*;
import com.atguigu.execption.GuiguException;
import com.atguigu.helper.SignHelper;
import com.atguigu.mapper.OrderInfoMapper;
import com.atguigu.result.ResultCodeEnum;
import com.atguigu.result.RetVal;
import com.atguigu.service.KafkaService;
import com.atguigu.service.OrderDerateService;
import com.atguigu.service.OrderDetailService;
import com.atguigu.service.OrderInfoService;
import com.atguigu.util.AuthContextHolder;
import com.atguigu.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单信息 服务实现类
 * </p>
 *
 * @author long
 * @since 2025-04-18
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
@Autowired
private UserFeignClient userFeignClient;
    @Autowired
    private AccountFeignClient accountFeignClient;
    @Autowired
    private AlbumFeignClient albumFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public OrderInfoVo confirmOrder(TradeVo tradeVo) {
        Long userId = AuthContextHolder.getUserId();
        UserInfoVo userInfoVo = userFeignClient.getUserById(userId).getData();
        BigDecimal finalPrice = new BigDecimal("0");
        BigDecimal deratePrice = new BigDecimal("0.00");
        BigDecimal originalPrice = new BigDecimal("0.00");
        List<OrderDetailVo> orderDetailVoList = new ArrayList<>();
        List<OrderDerateVo> orderDerateVoList = new ArrayList<>();
        //购买整个专辑
        if (tradeVo.getItemType().equals(SystemConstant.BUY_ALBUM)) {
            AlbumInfo albumInfo = albumFeignClient.getAlbumInfoById(tradeVo.getItemId()).getData();
            originalPrice = albumInfo.getPrice();
            //如果当前用户不是VIP会员
            if (userInfoVo.getIsVip() == 0) {
                //如果专辑可以打折 -1为不打折
                if (albumInfo.getDiscount().intValue() != -1) {
                    //打折金额计算 如:100*(10-8.5)/10=15
                    deratePrice = originalPrice.multiply(new BigDecimal(10).subtract(albumInfo.getDiscount()))
                            .divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                }
                finalPrice = originalPrice.subtract(deratePrice);
            } else {
                //如果专辑可以打折 -1为不打折
                if (albumInfo.getDiscount().intValue() != -1) {
                    //打折金额计算 如:100*(10-8.5)/10=15
                    deratePrice = originalPrice.multiply(new BigDecimal(10).subtract(albumInfo.getVipDiscount()))
                            .divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                }
                finalPrice = originalPrice.subtract(deratePrice);
            }
            //订单明细
            OrderDetailVo orderDetailVo = new OrderDetailVo();
            orderDetailVo.setItemId(tradeVo.getItemId());
            orderDetailVo.setItemName(albumInfo.getAlbumTitle());
            orderDetailVo.setItemUrl(albumInfo.getCoverUrl());
            orderDetailVo.setItemPrice(albumInfo.getPrice());
            orderDetailVoList.add(orderDetailVo);
            //订单减免信息
            if(deratePrice.intValue()!=0){
                OrderDerateVo orderDerateVo = new OrderDerateVo();
                orderDerateVo.setDerateType(SystemConstant.ALBUM_DISCOUNT);
                orderDerateVo.setDerateAmount(deratePrice);
                orderDerateVoList.add(orderDerateVo);
            }
        }
        //购买多个声音
        else if(tradeVo.getItemType().equals(SystemConstant.BUY_TRACK)){
            if(tradeVo.getTrackCount()<0) throw new GuiguException(ResultCodeEnum.ARGUMENT_VALID_ERROR);
            List<TrackInfo> prepareToBuyTrackList = albumFeignClient.getTrackListPrepareToBuy(tradeVo.getItemId(), tradeVo.getTrackCount()).getData();
            //该声音列表所有声音的专辑id都是一样的
            AlbumInfo albumInfo = albumFeignClient.getAlbumInfoById(prepareToBuyTrackList.get(0).getAlbumId()).getData();
            if(tradeVo.getTrackCount()>0){
                originalPrice = albumInfo.getPrice().multiply(new BigDecimal(tradeVo.getTrackCount()));
                finalPrice=originalPrice;
            }else{
                originalPrice = albumInfo.getPrice();
                finalPrice=originalPrice;
            }
            //订单明细
            orderDetailVoList=prepareToBuyTrackList.stream().map(prepareToBuy->{
                OrderDetailVo orderDetailVo = new OrderDetailVo();
                orderDetailVo.setItemId(prepareToBuy.getId());
                orderDetailVo.setItemName(prepareToBuy.getTrackTitle());
                orderDetailVo.setItemUrl(prepareToBuy.getCoverUrl());
                orderDetailVo.setItemPrice(albumInfo.getPrice());
                return orderDetailVo;
            }).collect(Collectors.toList());

        }
        //购买VIP会员
        else if(tradeVo.getItemType().equals(SystemConstant.BUY_VIP)){
            VipServiceConfig vipConfig = userFeignClient.getVipConfig(tradeVo.getItemId());
            originalPrice = vipConfig.getPrice();
            deratePrice=originalPrice.subtract(vipConfig.getDiscountPrice());
            finalPrice=vipConfig.getDiscountPrice();
            OrderDetailVo orderDetailVo = new OrderDetailVo();
            orderDetailVo.setItemId(tradeVo.getItemId());
            orderDetailVo.setItemName("VIP会员:"+vipConfig.getName());
            orderDetailVo.setItemUrl(vipConfig.getImageUrl());
            orderDetailVo.setItemPrice(finalPrice);
            orderDetailVoList.add(orderDetailVo);
            //订单减免信息
            if(deratePrice.intValue()!=0){
                OrderDerateVo orderDerateVo = new OrderDerateVo();
                orderDerateVo.setDerateType(SystemConstant.ORDER_DERATE_VIP_SERVICE_DISCOUNT);
                orderDerateVo.setDerateAmount(deratePrice);
                orderDerateVoList.add(orderDerateVo);
            }
        }
        //生成一个tradeNo 防止订单重复提交
        String tradeNoKey="user:trade:" + userId;
        String tradeNo= UUID.randomUUID().toString().replaceAll("-","");
        redisTemplate.opsForValue().set(tradeNoKey,tradeNo);

        OrderInfoVo orderInfoVo = new OrderInfoVo();
        orderInfoVo.setItemType(tradeVo.getItemType());
        orderInfoVo.setOriginalAmount(originalPrice);
        orderInfoVo.setDerateAmount(deratePrice);
        orderInfoVo.setOrderAmount(finalPrice);
        //防止一个tradeNo
        orderInfoVo.setTradeNo(tradeNo);
        orderInfoVo.setOrderDerateVoList(orderDerateVoList);
        orderInfoVo.setOrderDetailVoList(orderDetailVoList);
        orderInfoVo.setTimestamp(SignHelper.getTimestamp());
        orderInfoVo.setPayWay("");

        //生成一个签名
        Map paramMap = JSON.parseObject(JSON.toJSONString(orderInfoVo), Map.class);
        String sign = SignHelper.getSign(paramMap);
        orderInfoVo.setSign(sign);
        return orderInfoVo;

    }

    @Override
    public Map<String, Object>
    submitOrder(OrderInfoVo orderInfoVo) {
        Long userId = AuthContextHolder.getUserId();
        //校验签名是否被篡改
        Map paramMap = JSON.parseObject(JSON.toJSONString(orderInfoVo), Map.class);
        paramMap.put("payWay", "");
        SignHelper.checkSign(paramMap);

        String tradeNo = orderInfoVo.getTradeNo();
        String tradeNoKey="user:trade:" + userId;
        String script = "if(redis.call('get', KEYS[1]) == ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";
        Boolean flag =(Boolean) redisTemplate.execute(new DefaultRedisScript(script, Boolean.class), Arrays.asList(tradeNoKey), tradeNo);
        //如果flag为false 代表已经删除过了--已经提交过了
        if(!flag){
            //不能重复提交
            throw new GuiguException(ResultCodeEnum.REPEAT_SUBMIT_ORDER);
        }
        String orderNo=UUID.randomUUID().toString().replaceAll("-","");
        //账户余额支付
        if(SystemConstant.ACCOUNT_BALANCES.equals(orderInfoVo.getPayWay())){
            try {
                AccountLockVo accountLockVo = new AccountLockVo();
                accountLockVo.setOrderNo(orderNo);
                accountLockVo.setUserId(userId);
                accountLockVo.setAmount(orderInfoVo.getOrderAmount());
                accountLockVo.setContent(orderInfoVo.getOrderDetailVoList().get(0).getItemName());
                //锁定账号余额
                RetVal retVal = accountFeignClient.checkAndLock(accountLockVo);
                if(retVal.getCode()!=200){
                    throw new GuiguException(retVal.getCode(),retVal.getMessage());
                }
                saveOrderInfo(orderInfoVo,orderNo);





//                购买vip
                String itemType = orderInfoVo.getItemType();
                if(itemType.equals("1003")){
                    userFeignClient.changeVip(userId);
                }





                //支付成功减库存
                kafkaService.sendMessage(KafkaConstant.DEDUCT_LOCK_ACCOUNT_QUEUE,orderNo);

            } catch (GuiguException e) {
                e.printStackTrace();
                throw new GuiguException(e.getCode(),e.getMessage());
            } catch (Exception e){
                e.printStackTrace();
                //异常手动解锁账户
                kafkaService.sendMessage(KafkaConstant.UNLOCK_ACCOUNT_QUEUE,orderNo);
            }
        }else{
            //其他支付方式
            saveOrderInfo(orderInfoVo,orderNo);
        }
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("orderNo",orderNo);
        return retMap;

    }

    @Override
    public void cancelOrder(Long orderId) {
        OrderInfo orderInfo = getById(orderId);
        if(SystemConstant.ORDER_UNPAID.equals(orderInfo.getOrderStatus())){
            orderInfo.setOrderStatus(SystemConstant.ORDER_STATUS_CANCEL);
            updateById(orderInfo);
        }
    }

    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private OrderDerateService orderDerateService;
    private void saveOrderInfo(OrderInfoVo orderInfoVo, String orderNo) {
        Long userId = AuthContextHolder.getUserId();
        //保存订单基本信息
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(orderInfoVo,orderInfo);
        orderInfo.setOrderNo(orderNo);
        String orderTitle = orderInfoVo.getOrderDetailVoList().get(0).getItemName();

        orderInfo.setOrderTitle(orderTitle);
        orderInfo.setUserId(userId);
        orderInfo.setOrderStatus(SystemConstant.ORDER_UNPAID);
        save(orderInfo);
        //保存订单明细信息
        List<OrderDetailVo> orderDetailVoList = orderInfoVo.getOrderDetailVoList();
        List<OrderDetail> orderDetailList = orderDetailVoList.stream().map(orderDetailVo -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(orderDetailVo, orderDetail);
            orderDetail.setOrderId(orderInfo.getId());
            return orderDetail;
        }).collect(Collectors.toList());
        orderDetailService.saveBatch(orderDetailList);
        //保存订单减免信息
        List<OrderDerateVo> orderDerateVoList = orderInfoVo.getOrderDerateVoList();
        if(!CollectionUtils.isEmpty(orderDerateVoList)){
            List<OrderDerate> orderDerateList = orderDerateVoList.stream().map(orderDerateVo -> {
                OrderDerate orderDerate = new OrderDerate();
                BeanUtils.copyProperties(orderDerateVo, orderDerate);
                orderDerate.setOrderId(orderInfo.getId());
                return orderDerate;
            }).collect(Collectors.toList());
            orderDerateService.saveBatch(orderDerateList);
        }
        //如果采用余额支付
        if(SystemConstant.ACCOUNT_BALANCES.equals(orderInfoVo.getPayWay())){
            afterPaySuccess(orderNo);
        }else{
            //订单延迟自动取消
            sendDelayMessage(orderInfo.getId());
        }
    }
@Autowired
private RedissonClient redissonClient;
    private void sendDelayMessage(Long orderId) {
        RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(KafkaConstant.QUEUE_ORDER_CANCEL);
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        delayedQueue.offer(orderId.toString(),KafkaConstant.DELAY_TIME, TimeUnit.SECONDS);

    }

    @Autowired
private KafkaService kafkaService;
    private void afterPaySuccess(String orderNo) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderNo,orderNo);
        OrderInfo updateOrderInfo = new OrderInfo();
        updateOrderInfo.setOrderStatus(SystemConstant.ORDER_PAID);
        update(updateOrderInfo,wrapper);
        //2.保存用户支付记录 要传的参数后面再写
        OrderInfo orderInfo=getOrderAndDetail(orderNo);
        List<Long> itemIdList = orderInfo.getOrderDetailList().stream().map(OrderDetail::getItemId).collect(Collectors.toList());
        UserPaidRecordVo userPaidRecordVo = new UserPaidRecordVo();
        userPaidRecordVo.setUserId(orderInfo.getUserId());
        userPaidRecordVo.setOrderNo(orderNo);
        userPaidRecordVo.setItemType(orderInfo.getItemType());
        userPaidRecordVo.setItemIdList(itemIdList);
        kafkaService.sendMessage(KafkaConstant.USER_PAID_QUEUE,JSON.toJSONString(userPaidRecordVo));
        //3.更新购买专辑数量
        if(orderInfo.getItemType().equals(SystemConstant.BUY_ALBUM)||orderInfo.getItemType().equals(SystemConstant.BUY_TRACK)){
            Long albumId=0L;
            if(orderInfo.getItemType().equals(SystemConstant.BUY_ALBUM)){
                albumId=itemIdList.get(0);
            }else{
                TrackInfo trackInfo = albumFeignClient.getTrackInfoById(itemIdList.get(0)).getData();
                albumId = trackInfo.getAlbumId();
            }
            AlbumStatMqVo albumStatMqVo = new AlbumStatMqVo();
            albumStatMqVo.setBusinessNo(UUID.randomUUID().toString().replaceAll("-", ""));
            albumStatMqVo.setAlbumId(albumId);
            albumStatMqVo.setStatType(SystemConstant.BUY_NUM_ALBUM);
            kafkaService.sendMessage(KafkaConstant.UPDATE_ALBUM_BUY_NUM_QUEUE,JSON.toJSONString(albumStatMqVo));
        }
    }
@Override
    public OrderInfo getOrderAndDetail(String orderNo) {
        OrderInfo orderInfo = getOne(new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        List<OrderDetail> orderDetailList = orderDetailService.list(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderInfo.getId()));
        List<OrderDerate> orderDerateList = orderDerateService.list(new LambdaQueryWrapper<OrderDerate>().eq(OrderDerate::getOrderId, orderInfo.getId()));
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.setOrderDerateList(orderDerateList);
        orderInfo.setOrderStatusName(getOrderStatusName(orderInfo.getOrderStatus()));
        orderInfo.setPayWay(getPayWayName(orderInfo.getPayWay()));
        return orderInfo;
    }

    @Override
    public void getUserOrderByPage(IPage<OrderInfo> pageParam) {
        Long userId = AuthContextHolder.getUserId();
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getUserId,userId);
        page(pageParam,wrapper);
    }

    private String getPayWayName(String payWay) {
        String payWayName = "";
        if (payWay.equals(SystemConstant.ORDER_PAY_WAY_WEIXIN)) {
            payWayName = "微信";
        } else if (payWay.equals(SystemConstant.ORDER_PAY_WAY_ALIPAY)) {
            payWayName = "支付宝";
        } else {
            payWayName = "余额";
        }
        return payWayName;
    }

    private String getOrderStatusName(String orderStatus) {
        String orderStatusName = "";
        if (orderStatus.equals(SystemConstant.ORDER_UNPAID)) {
            orderStatusName = "待支付";
        } else if (orderStatus.equals(SystemConstant.ORDER_PAID)) {
            orderStatusName = "已支付";
        } else {
            orderStatusName = "已取消";
        }
        return orderStatusName;
    }


}
