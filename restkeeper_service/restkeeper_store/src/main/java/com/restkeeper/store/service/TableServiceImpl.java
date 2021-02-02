package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.mapper.TableMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;

@org.springframework.stereotype.Service("tableService")
@Service(version = "1.0.0",protocol = "dubbo")
public class TableServiceImpl extends ServiceImpl<TableMapper, Table> implements ITableService {
    @Override
    public boolean add(Table table) {
        checkNameExist(table.getTableName());
        return this.save(table);
    }

    @Override
    public IPage<Table> queryPageByAreaId(int pageNum, int pageSize, String areaId) {
        IPage<Table> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Table> queryWrapper = new QueryWrapper<>();
        if (!areaId.equals("all")){
            queryWrapper.lambda().eq(Table::getAreaId, areaId);
        }
        queryWrapper.lambda().orderByDesc(Table::getLastUpdateTime);
        return this.page(page, queryWrapper);
    }

    @Override
    public Integer countTableByStatus(String areaId, Integer status) {
        QueryWrapper<Table> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Table::getAreaId, areaId).eq(Table::getStatus, status);
        return this.baseMapper.selectCount(queryWrapper);
    }

    private void checkNameExist(String tableName) {
        QueryWrapper<Table> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(Table::getTableId).eq(Table::getTableName, tableName);
        Integer count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new RuntimeException("此桌名已经存在");
        }
    }
}
