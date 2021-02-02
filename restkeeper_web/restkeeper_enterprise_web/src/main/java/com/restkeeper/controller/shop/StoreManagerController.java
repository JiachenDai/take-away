package com.restkeeper.controller.shop;

import com.restkeeper.response.vo.PageVO;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.shop.service.IStoreManagerService;
import com.restkeeper.vo.shop.StoreManagerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/storeManager")
@Api(tags = "门店管理员接口")
public class StoreManagerController {

    @Reference(version = "1.0.0", check = false)
    private IStoreManagerService storeManagerService;

    @ApiOperation(value = "查询分页数据")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页码", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "分大小", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "name", value = "店长姓名", required = false, dataType = "String")})
    @PostMapping("/pageList/{page}/{pageSize}")
    public PageVO<StoreManager> findListByPage(@PathVariable("page") Integer page, @PathVariable("pageSize") Integer pageSize, @RequestParam String criteria) {
        return new PageVO<>(storeManagerService.queryPageByCriteria(page, pageSize, criteria));
    }


    @PostMapping("/add")
    @ApiOperation("添加店长")
    public boolean add(@RequestBody StoreManagerVO storeManagerVO) {
        return storeManagerService.addStoreManager(storeManagerVO.getName(),
                storeManagerVO.getPhone(), storeManagerVO.getStoreIds());
    }

    /**
     * 门店管理员修改接口
     */
    @ApiOperation(value = "更新数据")
    @PutMapping(value = "/update")
    public boolean update(@RequestBody StoreManagerVO storeManagerVO) {
        return storeManagerService.updateStoreManager(storeManagerVO.getId(),
                storeManagerVO.getName(),
                storeManagerVO.getPhone(),
                storeManagerVO.getStoreIds());
    }

    /**
     * 删除门店管理员
     */
    @ApiOperation(value = "删除数据")
    @DeleteMapping(value = "/del/{id}")
    public boolean delete(@PathVariable String id) {
        return storeManagerService.deleteStoreManager(id);
    }

    /**
     * 停用
     */
    @ApiOperation(value = "门店管理员停用")
    @PutMapping(value = "/pause/{id}")
    public boolean pause(@PathVariable String id) {
        return storeManagerService.pauseStoreManager(id);
    }

}