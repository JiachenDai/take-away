package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.DishFlavor;
import com.restkeeper.store.mapper.DishMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service(version = "1.0.0", protocol = "dubbo")
@org.springframework.stereotype.Service("dishService")
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements IDishService{

    @Autowired
    @Qualifier("dishFlavorService")
    private IDishFlavorService dishFlavorService;

    @Override
    @Transactional
    public boolean save(Dish dish, List<DishFlavor> dishFlavorList) {
        try {
            //保存菜品相关信息
            this.save(dish);
            //保存口味
            for (DishFlavor dishFlavor : dishFlavorList) {
                dishFlavor.setDishId(dish.getId());
            }
            //批量插入
            dishFlavorService.saveBatch(dishFlavorList);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean update(Dish dish, List<DishFlavor> dishFlavorList) {
        try {
            //进行菜品基本信息更新
            this.updateById(dish);
            //进行关联的口味信息更新
            QueryWrapper<DishFlavor> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DishFlavor::getDishId, dish.getId());
            dishFlavorService.remove(queryWrapper);
            //新增口味关联
            dishFlavorList.forEach(dishFlavor -> dishFlavor.setDishId(dish.getId()));
            dishFlavorService.saveBatch(dishFlavorList);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public List<Map<String, Object>> findEnableDishListInfo(String categoryId, String name) {
        QueryWrapper<Dish> queryMapper = new QueryWrapper<>();
        queryMapper.lambda().select(Dish::getId, Dish::getName, Dish::getStatus, Dish::getPrice);
        if (StringUtils.isNotEmpty(categoryId)){
            queryMapper.lambda().eq(Dish::getCategoryId, categoryId);
        }
        if (StringUtils.isNotEmpty(name)){
            queryMapper.lambda().eq(Dish::getName, name);
        }
        queryMapper.lambda().eq(Dish::getStatus, SystemCode.ENABLED);
        return this.listMaps(queryMapper);
    }
}
