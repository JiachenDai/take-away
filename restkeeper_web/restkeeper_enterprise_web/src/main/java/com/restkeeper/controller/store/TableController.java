package com.restkeeper.controller.store;

import com.restkeeper.response.vo.PageVO;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.entity.TableArea;
import com.restkeeper.store.service.ITableAreaService;
import com.restkeeper.store.service.ITableService;
import com.restkeeper.vo.store.TableVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Api(tags = {"区域管理"})
@RequestMapping("/table")
public class TableController {

    @Reference(version = "1.0.0", check = false)
    private ITableAreaService tableAreaService;

    @Reference(version = "1.0.0", check = false)
    private ITableService tableService;

    @ApiOperation(value = "添加区域")
    @PostMapping("/addArea")
    public boolean addArea(@RequestParam("name") String name) {
        TableArea tableArea = new TableArea();
        tableArea.setAreaName(name);
        return tableAreaService.add(tableArea);
    }

    @ApiOperation("添加桌台")
    @PostMapping("/addTable")
    public boolean addTable(@RequestBody TableVO tableVO){
        Table table = new Table();
        BeanUtils.copyProperties(tableVO,table);
        return tableService.add(table);
    }

    @ApiOperation(value = "根据区域id检索桌台")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "areaId", value = "区域Id", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "每页数量", required = true, dataType = "Integer")})
    @GetMapping("/search/{areaId}/{page}/{pageSize}")
    public PageVO<Table> queryByArea(@PathVariable String areaId, @PathVariable int page, @PathVariable int pageSize){
        return new PageVO<Table>(tableService.queryPageByAreaId(page,pageSize,areaId));
    }

}
