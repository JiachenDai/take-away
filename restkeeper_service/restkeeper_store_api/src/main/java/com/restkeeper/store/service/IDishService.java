package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.DishFlavor;

import java.util.List;
import java.util.Map;

public interface IDishService extends IService<Dish> {

    //添加菜品
    boolean save(Dish dish, List<DishFlavor> dishFlavorList);

    //修改菜品
    boolean update(Dish dish, List<DishFlavor> dishFlavorList);

    //根据分类信息和菜品的名称查询菜品列表
    List<Map<String, Object>> findEnableDishListInfo(String categoryId, String name);
}
