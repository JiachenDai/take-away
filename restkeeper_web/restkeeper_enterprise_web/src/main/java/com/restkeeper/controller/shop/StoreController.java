package com.restkeeper.controller.shop;

import com.restkeeper.constants.SystemCode;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.service.IStoreService;
import com.restkeeper.utils.Result;
import com.restkeeper.vo.shop.AddStoreVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Path;
import java.util.List;

@Slf4j
@RestController
@Api(tags = { "门店信息" })
@RequestMapping("/store")
public class StoreController {

    @Reference(version = "1.0.0", check = false)
    private IStoreService storeService;

    /**
     * 分页数据
     */
    @ApiOperation(value = "分页查询所有门店")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "分大小", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "name", value = "门店名称", required = false, dataType = "String") })
    @GetMapping(value = "/pageList/{page}/{pageSize}")
    public PageVO<Store> findListByPage(@PathVariable int page,
                                        @PathVariable int pageSize,
                                        @RequestParam(value = "name",required = false) String name) {
        return new PageVO<Store>(storeService.queryPageByName(page, pageSize,name));
    }


    /*
    新增门店
     */
    @ApiOperation(value = "新增门店")
    @PostMapping("/add")
    public boolean add(@RequestBody AddStoreVO addStoreVO){
        Store store = new Store();
        BeanUtils.copyProperties(addStoreVO, store);
        boolean flag = storeService.save(store);
        return flag;
    }

    /*
    停用门店
    但是如下代码会抛出找不到多租户id的异常，是因为下面代码进行了两次dubbo调用的原因，在第二次调用的时候上下文参数中的值丢失了
     */
    @ApiOperation("停用门店")
    @PutMapping("/forbidden/{id}")
    public boolean forbidden(@PathVariable("id") String id){
        if (StringUtils.isEmpty(id)){
            return false;
        }
        Store store = storeService.getById(id);
        store.setStatus(SystemCode.FORBIDDEN);
        return storeService.updateById(store);
    }


    //获取有效的省份信息
    @GetMapping("/listProvince")
    @ApiOperation(value = "获取有效省份信息")
    public List<String> listProvince(){
        return storeService.getAllProvince();
    }

    @ApiOperation(value = "根据省份获取门店列表")
    @GetMapping("/getStoreByProvince/{province}")
    public List<StoreDTO> getStoreByProvince(@PathVariable String province) {
        return storeService.getStoreByProvince(province);
    }

    @ApiOperation(value = "获取当前商户管理的门店信息")
    @GetMapping(value = "/listManagerStores")
    public List<StoreDTO> getStoreListByManagerId(){
        return storeService.getStoreListByManagerId();
    }

    @ApiOperation(value = "门店切换")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storeId", value = "门店Id", dataType = "String")})
    @GetMapping(value = "/switchStore/{storeId}")
    public Result switchStore(@PathVariable("storeId") String storeId){
        return storeService.switchStore(storeId);
    }
}
