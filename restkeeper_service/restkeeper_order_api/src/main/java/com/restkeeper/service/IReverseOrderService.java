package com.restkeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.entity.ReverseOrder;

public interface IReverseOrderService extends IService<ReverseOrder> {

    boolean reverse(ReverseOrder reverseOrder);

}
