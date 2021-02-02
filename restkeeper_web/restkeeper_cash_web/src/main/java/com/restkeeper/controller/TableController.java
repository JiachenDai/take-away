package com.restkeeper.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.entity.TableLog;
import com.restkeeper.store.service.ITableAreaService;
import com.restkeeper.store.service.ITableLogService;
import com.restkeeper.store.service.ITableService;
import com.restkeeper.vo.TablePanelVO;
import com.restkeeper.vo.TableVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/table")
@Api(tags = {"收银端区域桌台接口"})
public class TableController {
    @Reference(version = "1.0.0",check = false)
    private ITableAreaService tableAreaService;

    @Reference(version = "1.0.0",check = false)
    private ITableLogService tableLogService;


    @Reference(version = "1.0.0",check = false)
    private ITableService tableService;

    @ApiOperation(value = "区域列表接口")
    @GetMapping("/listTableArea")
    public List<Map<String,Object>> list(){
        return tableAreaService.listTableArea();
    }

    @ApiOperation(value = "开桌")
    @PutMapping("/openTable/{tableId}/{numbers}")
    public boolean openTable(@PathVariable String tableId, @PathVariable Integer numbers){
        TableLog tableLog =new TableLog();
        tableLog.setTableId(tableId);
        tableLog.setUserNumbers(numbers);
        return tableLogService.openTable(tableLog);
    }

    @ApiOperation(value = "桌台面板")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "areaId", value = "区域Id", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "每页数量", required = true, dataType = "Integer")})
    @GetMapping("/search/{areaId}/{page}/{pageSize}")
    public TablePanelVO queryByArea(@PathVariable String areaId, @PathVariable int page, @PathVariable int pageSize){
        if (StringUtils.isEmpty(areaId)){
            throw new RuntimeException("区域id为空");
        }
        TablePanelVO tablePanelVO = new TablePanelVO();
        //桌台统计信息
        Integer free = tableService.countTableByStatus(areaId, SystemCode.TABLE_STATUS_FREE);
        Integer opened = tableService.countTableByStatus(areaId, SystemCode.TABLE_STATUS_OPEND);
        Integer locked = tableService.countTableByStatus(areaId, SystemCode.TABLE_STATUS_LOCKED);
        tablePanelVO.setFreeNumbers(free);
        tablePanelVO.setLockedNumbers(locked);
        tablePanelVO.setOpenedNumbers(opened);
        //分页查询

        IPage<Table> tableIPage = tableService.queryPageByAreaId(page, pageSize, areaId);
        List<Table> records = tableIPage.getRecords();
        List<TableVO> tableVOS = new ArrayList<>();
        for (Table table : records) {
            TableVO tableVO = new TableVO();
            tableVO.setTableId(table.getTableId());
            tableVO.setTableName(table.getTableName());
            if (table.getStatus() == SystemCode.TABLE_STATUS_OPEND){
                //获取tableLog
                TableLog openTableLog = tableLogService.getOpenTableLog(table.getTableId());
                tableVO.setCreateTime(openTableLog.getCreateTime());
                tableVO.setUserNumbers(openTableLog.getUserNumbers());
            }
            tableVOS.add(tableVO);
        }
        PageVO<TableVO> pageVO = new PageVO<>(tableIPage, tableVOS);
        tablePanelVO.setTablePage(pageVO);
        return tablePanelVO;
    }

}
