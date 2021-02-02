package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.TableArea;

import java.util.List;
import java.util.Map;

public interface ITableAreaService extends IService<TableArea> {

    //新增区域
    boolean add(TableArea tableArea);

    //获取所有区域
    List<Map<String, Object>> listTableArea();
}
