package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.operator.entity.SysDictionary;
import com.restkeeper.operator.service.ISysDictService;
import com.restkeeper.store.entity.Remark;
import com.restkeeper.store.mapper.RemarkMapper;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service("remarkService")
@Service(version = "1.0.0",protocol = "dubbo")
public class RemarkServiceImpl extends ServiceImpl<RemarkMapper, Remark> implements IRemarkService {

    @Reference(version = "1.0.0", check = false)
    private ISysDictService sysDictService;

    @Override
    public List<Remark> getRemarks() {
        //先查询remark表，如果没有再查询系统表
        List<Remark> list = this.list();
        if (list == null || list.size() == 0){
            list = new ArrayList<>();
            List<SysDictionary> dictionaryList = sysDictService.getDictionaryList(SystemCode.DICTIONARY_REMARK);
            for (SysDictionary sysDictionary : dictionaryList) {
                Remark remark = new Remark();
                remark.setRemarkName(sysDictionary.getDictName());
                remark.setRemarkValue(sysDictionary.getDictData());
                list.add(remark);
            }
        }

        return list;
    }

    @Override
    public boolean updateRemarks(List<Remark> remarks) {
        //先删除以前的备注，然后批量插入新的
        QueryWrapper<Remark> queryWrapper = new QueryWrapper<>();
        this.remove(queryWrapper);
        //批量插入新的
        return this.saveBatch(remarks);
    }
}
