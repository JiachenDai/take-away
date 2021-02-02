package com.restkeeper.controller;

import com.google.common.collect.Lists;
import com.restkeeper.constants.OrderPayType;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.dto.CreditDTO;
import com.restkeeper.dto.DetailDTO;
import com.restkeeper.entity.OrderDetailEntity;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.entity.ReverseOrder;
import com.restkeeper.service.IOrderService;
import com.restkeeper.service.IReverseOrderService;
import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.DishCategory;
import com.restkeeper.store.service.IDishService;
import com.restkeeper.store.service.ISetMealService;
import com.restkeeper.tenant.TenantContext;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import com.restkeeper.utils.SequenceUtils;
import com.restkeeper.vo.OrderDetailVO;
import com.restkeeper.vo.OrderVO;
import com.restkeeper.vo.PayVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.omg.SendingContext.RunTime;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Api(tags = {"订单接口"})
@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference(version = "1.0.0",check = false)
    private ISetMealService setMealService;

    @Reference(version = "1.0.0",check = false)
    private IOrderService orderService;

    @Reference(version = "1.0.0",check = false)
    private IDishService dishService;

    @Reference(version = "1.0.0",check = false)
    private IReverseOrderService reverseOrderService;

    @ApiOperation("下单")
    @PostMapping("/add")
    public Result addOrder(@RequestBody OrderVO orderVO) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setTableId(orderVO.getTableId());
        orderEntity.setPayStatus(SystemCode.ORDER_STATUS_NOTPAY);
        orderEntity.setOperatorName(TenantContext.getLoginUserName());
        orderEntity.setOrderSource(SystemCode.ORDER_SOURCE_STORE);
        orderEntity.setTotalAmount(orderVO.getTotalAmount());
        orderEntity.setPersonNumbers(orderVO.getPersonNumbers());
        orderEntity.setOrderRemark(orderVO.getOrderRemark());
        orderEntity.setCreateTime(LocalDateTime.now());

        List<OrderDetailEntity> orderDetails = Lists.newArrayList();

        orderVO.getDishes().forEach(dishVO->{
            OrderDetailEntity orderDetailEntity = new OrderDetailEntity();
            orderDetailEntity.setDishName(dishVO.getDishName());
            orderDetailEntity.setDishType(dishVO.getType());
            orderDetailEntity.setDishId(dishVO.getDishId());
            orderDetailEntity.setDishAmount(dishVO.getDishNumber()*dishVO.getPrice());
            orderDetailEntity.setDishNumber(dishVO.getDishNumber());
            orderDetailEntity.setFlavorRemark(dishVO.getFlavorList().toString());
            orderDetailEntity.setTableId(orderVO.getTableId());
            orderDetailEntity.setDishPrice(dishVO.getPrice());
            DishCategory dishCategory = null;
            if (orderDetailEntity.getDishType() == 1){
                dishCategory = dishService.getById(dishVO.getDishId()).getDishCategory();
            }else{
                dishCategory = setMealService.getById(dishVO.getDishId()).getCategory();
            }
            if (dishCategory!=null){
                orderDetailEntity.setDishCategoryName(dishCategory.getName());
            }
            orderDetails.add(orderDetailEntity);
        });
        orderEntity.setOrderDetails(orderDetails);
        String orderId = orderService.addOrder(orderEntity);
        Result result = new Result();
        result.setStatus(ResultCode.success);
        result.setData(orderId);
        return result;

    }

    @ApiOperation("加菜")
    @PostMapping("/plusDish/orderId/{orderId}")
    public Result plusDish(@PathVariable String orderId, @RequestBody List<OrderDetailVO> details){
        if (StringUtils.isEmpty(orderId)){
            throw new RuntimeException("订单id为空");
        }
        OrderEntity order = orderService.getById(orderId);
        if (order == null){
            throw new RuntimeException("不存在该订单");
        }
        //操作订单表修改总金额
        //操作订单详情表，添加加菜相关的信息
        List<OrderDetailEntity> orderDetailEntities = new ArrayList<>();
        int totalAmount = 0;
        for (OrderDetailVO detail : details) {
            OrderDetailEntity orderDetailEntity = new OrderDetailEntity();
            totalAmount += detail.getDishNumber() * detail.getPrice();
            //一堆set操作
            orderDetailEntity.setOrderId(order.getOrderId());
            orderDetailEntity.setOrderNumber(SequenceUtils.getSequenceWithPrefix(order.getOrderNumber()));
            orderDetailEntity.setTableId(order.getTableId());
            orderDetailEntity.setDetailStatus(detail.getStatus());
            orderDetailEntity.setAddRemark(detail.getDishRemark());
            orderDetailEntity.setDishId(detail.getDishId());
            orderDetailEntity.setDishType(detail.getType());
            orderDetailEntity.setDishName(detail.getDishName());
            orderDetailEntity.setDishPrice(detail.getPrice());
            orderDetailEntity.setDishNumber(detail.getDishNumber());
            orderDetailEntity.setDishAmount(detail.getDishNumber()*detail.getPrice());
            orderDetailEntity.setDishRemark(detail.getDishRemark());
            orderDetailEntity.setFlavorRemark(detail.getFlavorList().toString());
            Dish dish = dishService.getById(detail.getDishId());
            if(dish!=null){
                orderDetailEntity.setDishCategoryName(dish.getDishCategory().getName());
            }
            orderDetailEntities.add(orderDetailEntity);
        }
        order.setTotalAmount(order.getTotalAmount() + totalAmount);
        order.setOrderDetails(orderDetailEntities);
        orderId = orderService.addOrder(order);
        Result result = new Result();
        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setData(orderId);
        return result;
    }

    /**
     * 退菜
     * @param detailId
     * @param remarks
     * @return
     */
    @PostMapping("/returnDish/{detailId}")
    public boolean returnDish(@PathVariable String detailId, @RequestBody List<String> remarks){
        DetailDTO detailDTO = new DetailDTO();
        detailDTO.setRemarks(remarks);
        detailDTO.setDetailId(detailId);
        return  orderService.returnDish(detailDTO);
    }

    /**
     * 结账
     * @param payVO
     * @return
     */
    @PostMapping("/pay/orderId/{orderId}")
    public boolean pay(@PathVariable String orderId, @RequestBody PayVO payVO){
        OrderEntity orderEntity= orderService.getById(orderId);
        orderEntity.setPayAmount(payVO.getPayAmount());
        orderEntity.setSmallAmount(payVO.getSmallAmount());
        orderEntity.setPayStatus(SystemCode.ORDER_STATUS_PAYED);
        orderEntity.setPayType(payVO.getPayType());
        //挂账
        if (payVO.getPayType() == OrderPayType.CREDIT.getType()){
            CreditDTO creditDTO =new CreditDTO();
            creditDTO.setCreditId(payVO.getCreditId());
            creditDTO.setCreditAmount(payVO.getCreditAmount());
            creditDTO.setCreditUserName(payVO.getCreditUserName());
            return orderService.pay(orderEntity,creditDTO);
        }
        return orderService.pay(orderEntity);
    }

    /**
     * 换桌
     * @param orderId
     * @param targetTableId
     * @return
     */
    @GetMapping("/changeTable/{orderId}/{targetTableId}")
    public boolean changeTable(@PathVariable String orderId,@PathVariable String targetTableId){
        return orderService.changeTable(orderId,targetTableId);
    }

    /**
     * 反结账
     * @param orderId 反结账订单
     * @param remarks 反结账原因
     * @return
     */
    @PostMapping("/reverse/{orderId}")
    public boolean reverse(@PathVariable String orderId, @RequestBody  List<String> remarks){
        ReverseOrder reverseOrder=new ReverseOrder();
        reverseOrder.setOrderId(orderId);
        reverseOrder.setRemark(remarks.toString());
        return reverseOrderService.reverse(reverseOrder);
    }
}
