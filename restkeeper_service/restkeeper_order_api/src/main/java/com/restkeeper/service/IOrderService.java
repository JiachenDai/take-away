package com.restkeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.dto.CreditDTO;
import com.restkeeper.dto.DetailDTO;
import com.restkeeper.entity.OrderEntity;

public interface IOrderService extends IService<OrderEntity> {
    //下单
    String addOrder(OrderEntity orderEntity);

    //退菜
    boolean returnDish(DetailDTO detailDTO);

    //结账
    boolean pay(OrderEntity orderEntity);

    //挂账结账
    boolean pay(OrderEntity orderEntity, CreditDTO creditDTO);

    //换台
    boolean changeTable(String orderId, String targetTableId);

}
