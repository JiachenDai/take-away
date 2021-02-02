package com.restkeeper.controller.store;

import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.DishFlavor;
import com.restkeeper.store.service.IDishService;
import com.restkeeper.vo.store.DishFlavorVO;
import com.restkeeper.vo.store.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.omg.SendingContext.RunTime;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = { "菜品管理" })
@RestController
@RequestMapping("/dish")
public class DishController {

    @Reference(version = "1.0.0", check = false)
    private IDishService dishService;

    @PostMapping("/add")
    @ApiOperation("添加菜品")
    public boolean add(@RequestBody DishVO dishVO){
        //设置菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishVO, dish);

        //设置口味
        List<DishFlavorVO> dishFlavors = dishVO.getDishFlavors();
        List<DishFlavor> dishFlavorList = new ArrayList<>();
        for (DishFlavorVO dishFlavorVO : dishFlavors) {
            DishFlavor dishFlavor = new DishFlavor();
            dishFlavor.setFlavorName(dishFlavorVO.getFlavor());
            dishFlavor.setFlavorValue(dishFlavorVO.getFlavorData().toString());
            dishFlavorList.add(dishFlavor);
        }

        return dishService.save(dish, dishFlavorList);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品相关信息")
    public DishVO getDish(@PathVariable("id") String id){
        DishVO dishVO = new DishVO();
        if (StringUtils.isEmpty(id)){
            throw new RuntimeException("id 为空");
        }
        Dish dish = dishService.getById(id);
        if (dish == null){
            throw new RuntimeException("菜品不存在");
        }
        //完成基本信息的填充
        BeanUtils.copyProperties(dish, dishVO);
        //设置口味信息
        List<DishFlavor> flavorList = dish.getFlavorList();
        List<DishFlavorVO> dishFlavorVOList = new ArrayList<>();
        for (DishFlavor dishFlavor : flavorList) {
            DishFlavorVO dishFlavorVO = new DishFlavorVO();
            dishFlavorVO.setFlavor(dishFlavor.getFlavorName());
            //[加酸，加甜]
            String flavorValue = dishFlavor.getFlavorValue();
            //加酸，加甜
            String substring = flavorValue.substring(flavorValue.indexOf("[") + 1, flavorValue.indexOf("]"));
            if (StringUtils.isNotEmpty(substring)){
                String[] flavors = substring.split(",");
                List<String> flavorData = new ArrayList<>();
                for (String flavor : flavors) {
                    flavorData.add(flavor);
                }
                dishFlavorVO.setFlavorData(flavorData);
            }
            dishFlavorVOList.add(dishFlavorVO);
        }
        dishVO.setDishFlavors(dishFlavorVOList);
        return dishVO;
    }

    @PutMapping("/update")
    @ApiOperation("修改菜品")
    public boolean update(@RequestBody DishVO dishVO){
        //先更新菜品基本信息
        String id = dishVO.getId();
        Dish dish = dishService.getById(id);
        if (dish == null){
            throw new RuntimeException("该菜品不存在");
        }
        BeanUtils.copyProperties(dishVO,dish);

        //然后更新关联的口味
        List<DishFlavorVO> dishFlavors = dishVO.getDishFlavors();
        List<DishFlavor> dishFlavorList = new ArrayList<>();
        for (DishFlavorVO dishFlavorVO : dishFlavors) {
            DishFlavor dishFlavor = new DishFlavor();
            BeanUtils.copyProperties(dishFlavorVO, dishFlavor);
            dishFlavorList.add(dishFlavor);
        }
        return dishService.update(dish, dishFlavorList);
    }

    @ApiOperation(value = "查询可用的菜品列表")
    @GetMapping("/findEnableDishList/{categoryId}")
    public List<Map<String,Object>> findEnableDishList(@PathVariable String categoryId,
                                                       @RequestParam(value="name",required=false) String name){
        return dishService.findEnableDishListInfo(categoryId, name);
    }
}
