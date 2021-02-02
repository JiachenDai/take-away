package com.restkeeper.controller.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.shop.entity.Brand;
import com.restkeeper.shop.service.IBrandService;
import com.restkeeper.vo.shop.AddTBrandVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Api("品牌管理")
@RequestMapping("/brand")
public class BrandController {

    @Reference(version = "1.0.0", check = false)
    private IBrandService brandService;

    @ApiOperation(value = "分页查询所有品牌信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页码", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "分大小", required = false, dataType = "Integer")})
    @GetMapping("/pageList/{pageNum}/{pageSize}")
    public PageVO<Brand> pageList(@PathVariable("pageNum") Integer pageNum, @PathVariable("pageSize") Integer pageSize){
        return new PageVO<>(brandService.queryPage(pageNum, pageSize));
    }


    @ApiOperation("新增品牌")
    @PostMapping("/add")
    public boolean add(@RequestBody AddTBrandVO addTBrandVO){
        Brand brand = new Brand();
        BeanUtils.copyProperties(addTBrandVO, brand);
        return brandService.save(brand);
    }

    @ApiOperation(value = "品牌列表（下拉选择使用）")
    @GetMapping("/brandList")
    @ResponseBody
    public List<Map<String, Object>> list() {
        return brandService.getBrandList();
    }


}
