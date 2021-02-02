package com.restkeeper.controller.store;


import com.google.common.collect.Lists;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.store.entity.SetMeal;
import com.restkeeper.store.entity.SetMealDish;
import com.restkeeper.store.service.ISetMealService;
import com.restkeeper.vo.store.SetMealDishVO;
import com.restkeeper.vo.store.SetMealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@Slf4j
@Api(tags = {"套餐管理"})
@RequestMapping("/setMeal")
public class SetMealController {
    @Reference(version = "1.0.0", check = false)
    private ISetMealService setMealService;

    @ApiOperation("套餐分页查询")
    @GetMapping("/queryPage/{page}/{pageSize}")
    public PageVO<SetMeal> queryPage(@PathVariable("page") Integer page,
                                     @PathVariable("pageSize") Integer pageSize,
                                     @RequestParam(value = "name", required = false) String name) {
        return new PageVO<>(setMealService.queryPage(page, pageSize, name));
    }

    /**
     * 添加套餐
     * @param
     */
    @ApiOperation("添加套餐")
    @PostMapping
    public boolean add(@RequestBody SetMealVO setMealVO){
        //设置套餐信息
        SetMeal setMeal = new SetMeal();
        BeanUtils.copyProperties(setMealVO,setMeal);

        //设置菜品相关信息
        List<SetMealDish> setMealDishList = Lists.newArrayList();
        if (setMealVO.getDishList() != null){
            setMealVO.getDishList().forEach(d->{
                SetMealDish setMealDish = new SetMealDish();
                setMealDish.setIndex(0);
                setMealDish.setDishCopies(d.getCopies());
                setMealDish.setDishId(d.getDishId());
                setMealDish.setDishName(d.getDishName());
                setMealDishList.add(setMealDish);
            });
        }
        return setMealService.add(setMeal, setMealDishList);
    }

    @ApiOperation("根据id查询套餐信息")
    @GetMapping("/{id}")
    public SetMealVO getInfo(@PathVariable("id") String id){
        SetMealVO setMealVO = new SetMealVO();
        SetMeal setMeal = setMealService.getById(id);
        if (setMeal == null){
            throw new RuntimeException("套餐不存在");
        }
        BeanUtils.copyProperties(setMeal, setMealVO);
        //完成菜品列表的拷贝
        List<SetMealDish> dishList = setMeal.getDishList();
        List<SetMealDishVO> dishVOList = new ArrayList<>();
        for (SetMealDish setMealDish : dishList) {
            SetMealDishVO setMealDishVO = new SetMealDishVO();
            setMealDishVO.setDishId(setMealDish.getDishId());
            setMealDishVO.setDishName(setMealDish.getDishName());
            setMealDishVO.setCopies(setMealDish.getDishCopies());
            dishVOList.add(setMealDishVO);
        }
        setMealVO.setDishList(dishVOList);
        return setMealVO;
    }

    @PutMapping("/update")
    public boolean update(@RequestBody SetMealVO setMealVo){
        SetMeal setMeal = setMealService.getById(setMealVo.getId());
        BeanUtils.copyProperties(setMealVo,setMeal);
        setMeal.setDishList(null);
        List<SetMealDish> setMealDishList = Lists.newArrayList();
        if(setMealVo.getDishList() != null){
            setMealVo.getDishList().forEach(d->{
                SetMealDish setMealDish = new SetMealDish();
                setMealDish.setIndex(0);
                setMealDish.setDishCopies(d.getCopies());
                setMealDish.setDishId(d.getDishId());
                setMealDishList.add(setMealDish);
            });
        }
        setMeal.setDishList(setMealDishList);
        return setMealService.update(setMeal);
    }
}
