package com.restkeeper.operator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.operator.entity.SysDictionary;

import java.util.List;

public interface ISysDictService extends IService<SysDictionary> {

    //根据分类信息查询字典表
    List<SysDictionary> getDictionaryList(String category);
}
