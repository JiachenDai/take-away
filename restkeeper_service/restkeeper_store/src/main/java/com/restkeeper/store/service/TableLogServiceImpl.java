package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.entity.TableLog;
import com.restkeeper.store.mapper.TableLogMapper;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@org.springframework.stereotype.Service("tableLogService")
@Service(version = "1.0.0",protocol = "dubbo")
public class TableLogServiceImpl extends ServiceImpl<TableLogMapper, TableLog> implements ITableLogService {

    @Autowired
    @Qualifier("tableService")
    private ITableService tableService;


    @Override
    @Transactional
    public boolean openTable(TableLog tableLog) {
        //获取桌台信息
        Table table = tableService.getById(tableLog.getTableId());
        if (table == null){
            throw new RuntimeException("此桌台不存在");
        }
        //进行内容判断
        if (SystemCode.TABLE_STATUS_FREE != table.getStatus()){
            throw new RuntimeException("非空闲桌台不能开桌");
        }
        if (tableLog.getUserNumbers() > table.getTableSeatNumber()){
            throw new RuntimeException("人数大于桌台最大座位数");
        }
        //修改桌台状态
        table.setStatus(SystemCode.TABLE_STATUS_OPEND);
        tableService.updateById(table);
        //设置当前的开桌人和开桌时间
        tableLog.setUserId(RpcContext.getContext().getAttachment("loginUserName"));
        tableLog.setCreateTime(LocalDateTime.now());
        tableLog.setTableStatus(SystemCode.TABLE_STATUS_OPEND);
        return this.save(tableLog);
    }

    @Override
    public TableLog getOpenTableLog(String tableId) {
        QueryWrapper<TableLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableLog::getTableId, tableId).orderByDesc(TableLog::getCreateTime);
        return this.list(queryWrapper).get(0);
    }
}
