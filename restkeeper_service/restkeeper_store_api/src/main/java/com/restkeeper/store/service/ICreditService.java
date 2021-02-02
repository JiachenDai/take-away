package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditCompanyUser;
import com.restkeeper.store.entity.CreditLogs;

import java.util.List;

public interface ICreditService extends IService<Credit> {

    //新增挂账
    boolean add(Credit credit, List<CreditCompanyUser> users);

    //挂账列表查询实现
    IPage<Credit> queryPage(int pageNum, int pageSize, String name);

    //挂账回显
    Credit queryById(String id);

    //修改挂账
    boolean updateInfo(Credit credit, List<CreditCompanyUser> users);

}
