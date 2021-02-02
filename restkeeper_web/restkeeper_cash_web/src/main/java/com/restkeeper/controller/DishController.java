package com.restkeeper.controller;

import com.restkeeper.entity.DishEs;
import com.restkeeper.entity.SearchResult;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.service.IDishSearchService;
import com.restkeeper.store.service.ISellCalculationService;
import com.restkeeper.vo.DishPanelVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Api(tags = {"菜品搜索相关接口"})
@RestController
@RequestMapping("/dish")
public class DishController {

    @Reference(version = "1.0.0", check = false)
    private IDishSearchService dishSearchService;

    @Reference(version = "1.0.0", check = false)
    private ISellCalculationService sellCalculationService;



    @ApiOperation("根据商品编码完成商品搜索工作")
    @GetMapping("/queryByCode/{code}/{page}/{pageSize}")
    public PageVO<DishPanelVO> queryByCode(@PathVariable("code") String code,
                                           @PathVariable("page") int page,
                                           @PathVariable("pageSize") int pageSize){
        PageVO<DishPanelVO> pageResult = new PageVO<DishPanelVO>();

        SearchResult<DishEs> result = dishSearchService.searchDishByCode(code, page, pageSize);
        //设置结果的分页信息
        pageResult.setCounts(result.getTotal());
        pageResult.setPage(page);
        pageResult.setPages(result.getTotal() % pageSize == 0 ? result.getTotal() / pageSize : result.getTotal() / pageSize + 1);
        pageResult.setPagesize(pageSize);
        //进行list构造
        List<DishEs> records = result.getRecords();
        List<DishPanelVO> dishPanelVOList = new ArrayList<>();
        for (DishEs dishEs : records) {
            DishPanelVO dishPanelVO = new DishPanelVO();
            dishPanelVO.setDishId(dishEs.getId());
            dishPanelVO.setDishName(dishEs.getName());
            dishPanelVO.setType(dishEs.getType());
            dishPanelVO.setPrice(dishEs.getPrice());
            dishPanelVO.setImage(dishEs.getImage());
            dishPanelVO.setRemainder(sellCalculationService.getRemainderCount(dishEs.getId()));
            dishPanelVOList.add(dishPanelVO);
        }
        pageResult.setItems(dishPanelVOList);
        return pageResult;

    }
}
