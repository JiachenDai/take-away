package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.CreditLogs;

import java.time.LocalDateTime;
import java.util.List;

public interface ICreditLogService extends IService<CreditLogs> {

    //查询挂账明细列表
    IPage<CreditLogs> queryPage(int pageNum, int pageSize, String creditId);

    //根据挂账id和时间查询相关数据信息
    List<CreditLogs> list(String creditId, LocalDateTime start, LocalDateTime end);
}
