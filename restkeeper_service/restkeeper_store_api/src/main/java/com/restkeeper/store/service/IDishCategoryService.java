package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.DishCategory;

import java.util.List;
import java.util.Map;

public interface IDishCategoryService extends IService<DishCategory> {

    //新增分类
    boolean add(String name, int type);

    //分类修改
    boolean update(String id, String name);

    //分类信息的分页查询
    IPage<DishCategory> queryPage(int pageNum, int pageSize);

    /**
     * 根据分类获取下拉列表
     * @param type
     * @return
     */
    List<Map<String,Object>> findCategoryList(Integer type);
}
