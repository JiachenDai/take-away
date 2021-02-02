package com.restkeeper.operator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.operator.entity.OperatorUser;
import com.restkeeper.utils.Result;

public interface IOperatorUserService extends IService<OperatorUser> {
    //登录
    Result login(String loginName, String loginPass);
}
