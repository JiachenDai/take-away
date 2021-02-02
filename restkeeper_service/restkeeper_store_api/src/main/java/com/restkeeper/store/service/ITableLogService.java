package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.TableLog;

public interface ITableLogService extends IService<TableLog> {

    //开桌
    boolean openTable(TableLog tableLog);

    //根据tableId获取每个桌台的详细信息
    TableLog getOpenTableLog(String tableId);
}
