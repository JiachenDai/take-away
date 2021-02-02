package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.store.entity.SellCalculation;
import com.restkeeper.store.mapper.SellCalculationMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service("sellCalculationService")
@Service(version = "1.0.0",protocol = "dubbo")
public class SellCalculationServiceImpl extends ServiceImpl<SellCalculationMapper, SellCalculation> implements ISellCalculationService {
    @Override
    public Integer getRemainderCount(String dishId) {
        if (StringUtils.isEmpty(dishId)){
            throw new RuntimeException("菜品id为空");
        }
        QueryWrapper<SellCalculation> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SellCalculation::getDishId, dishId).select(SellCalculation::getRemainder);
        SellCalculation sellCalculation = this.getOne(queryWrapper);
        if (sellCalculation == null){
            return -1;
        }
        return sellCalculation.getRemainder();
    }

    @Override
    @Transactional
    public void decrease(String dishId, Integer dishNumber) {
        QueryWrapper<SellCalculation> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SellCalculation::getDishId, dishId);
        SellCalculation sellCalculation = this.getOne(queryWrapper);

        if (sellCalculation != null){
            int resultCount = sellCalculation.getRemainder() - dishNumber;
            if (resultCount < 0)
                resultCount = 0;
            sellCalculation.setRemainder(resultCount);
            this.updateById(sellCalculation);
        }
    }

    @Override
    public void add(String dishId, int dishNum) {
        QueryWrapper<SellCalculation> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SellCalculation::getDishId, dishId);
        SellCalculation sellCalculation = this.getOne(queryWrapper);
        sellCalculation.setRemainder(sellCalculation.getRemainder() + dishNum);
        this.updateById(sellCalculation);
    }


}
