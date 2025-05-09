package com.atguigu.controller;

import com.atguigu.entity.OrderInfo;
import com.atguigu.login.TingShuLogin;
import com.atguigu.result.RetVal;
import com.atguigu.service.OrderInfoService;
import com.atguigu.vo.OrderInfoVo;
import com.atguigu.vo.TradeVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 订单信息 前端控制器
 * </p>
 *
 * @author long
 * @since 2025-04-18
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderController {
    @Autowired
    private OrderInfoService orderInfoService;
//    http://127.0.0.1/api/order/orderInfo/confirmOrder
    @Operation(summary = "确认订单")
    @TingShuLogin
    @PostMapping("confirmOrder")
    public RetVal confirmOrder(@RequestBody TradeVo tradeVo){
        OrderInfoVo orderInfoVo = orderInfoService.confirmOrder(tradeVo);
        return RetVal.ok(orderInfoVo);

    }
    @TingShuLogin
    @Operation(summary = "提交订单")
    @PostMapping("submitOrder")
    public RetVal submitOrder(@RequestBody OrderInfoVo orderInfoVo)  {
        Map<String, Object> retMap = orderInfoService.submitOrder(orderInfoVo);
        return RetVal.ok(retMap);
    }
    @Operation(summary = "根据订单号获取订单信息")
    @GetMapping("getOrderInfo/{orderNo}")
    public RetVal<OrderInfo> getOrderInfo(@PathVariable String orderNo) {
        OrderInfo orderAndDetail = orderInfoService.getOrderAndDetail(orderNo);
        return RetVal.ok(orderAndDetail);
    }
//    http://127.0.0.1/api/order/orderInfo/getUserOrderByPage/1/10
@Operation(summary = "获取订单信息")
@TingShuLogin
@GetMapping("getUserOrderByPage/{pageNum}/{pageSize}")
public RetVal getUserOrderByPage(@PathVariable Long pageNum,@PathVariable Long pageSize) {
    IPage<OrderInfo> pageParam = new Page<>(pageNum,pageSize);
    orderInfoService.getUserOrderByPage(pageParam);
        return RetVal.ok(pageParam);
}

}
