package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.store.entity.TableArea;
import com.restkeeper.store.mapper.TableAreaMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service("tableAreaService")
@Service(version = "1.0.0",protocol = "dubbo")
public class TableAreaServiceImpl extends ServiceImpl<TableAreaMapper, TableArea> implements ITableAreaService {

    @Override
    @Transactional
    public boolean add(TableArea tableArea) {
        //区域名称防重
        checkNameExist(tableArea.getAreaName());
        return this.save(tableArea);
    }

    @Override
    public List<Map<String, Object>> listTableArea() {
        QueryWrapper<TableArea> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(TableArea::getAreaId, TableArea::getAreaName).orderByDesc(TableArea::getIndexNumber);
        return this.listMaps(queryWrapper);
    }

    private void checkNameExist(String areaName) {
        QueryWrapper<TableArea> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(TableArea::getAreaId).eq(TableArea::getAreaName, areaName);
        Integer count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new RuntimeException("该区域已存在");
        }
    }
}
