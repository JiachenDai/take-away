package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.SellCalculation;

public interface ISellCalculationService extends IService<SellCalculation> {
    //根据菜品id获取剩余的沽清数量
    Integer getRemainderCount(String dishId);

    void decrease(String dishId, Integer dishNumber);

    void add(String dishId, int dishNum);

}
