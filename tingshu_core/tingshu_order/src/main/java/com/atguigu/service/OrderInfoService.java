package com.atguigu.service;

import com.atguigu.entity.OrderInfo;
import com.atguigu.vo.OrderInfoVo;
import com.atguigu.vo.TradeVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 订单信息 服务类
 * </p>
 *
 * @author long
 * @since 2025-04-18
 */
public interface OrderInfoService extends IService<OrderInfo> {

    OrderInfoVo confirmOrder(TradeVo tradeVo);

    Map<String, Object> submitOrder(OrderInfoVo orderInfoVo);
    void cancelOrder(Long orderId);

    OrderInfo getOrderAndDetail(String orderNo);

    void getUserOrderByPage(IPage<OrderInfo> pageParam);
}
