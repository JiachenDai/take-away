package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.Table;

public interface ITableService extends IService<Table> {
    //新增桌台
    boolean add(Table table);

    //查询桌台列表
    IPage<Table> queryPageByAreaId(int pageNum, int pageSize, String areaId);

    //根据区域id和状态来获取到状态相关的数量值
    Integer countTableByStatus(String areaId, Integer status);
}
