package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.restkeeper.store.entity.SetMeal;
import com.restkeeper.store.entity.SetMealDish;
import com.restkeeper.store.mapper.SetMealMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service(version = "1.0.0", protocol = "dubbo")
@org.springframework.stereotype.Service("setMealService")
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, SetMeal> implements ISetMealService {

    @Autowired
    private ISetMealDishService setMealDishService;

    @Override
    public IPage<SetMeal> queryPage(int pageNum, int pageSize, String name) {
        IPage<SetMeal> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SetMeal> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)){
            queryWrapper.lambda().like(SetMeal::getName, name);
        }
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public boolean add(SetMeal setMeal, List<SetMealDish> setMealDishes) {
        try {
            this.save(setMeal);
            for (SetMealDish setMealDish : setMealDishes) {
                setMealDish.setSetMealId(setMeal.getId());
                setMealDish.setIndex(0);
            }
            setMealDishService.saveBatch(setMealDishes);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public boolean update(SetMeal setMeal) {
        try {
            //这是数据库中的数据
            SetMeal setMealFromDB = this.getById(setMeal.getId());
            if (setMealFromDB == null){
                throw new RuntimeException("不存在该套餐");
            }
            BeanUtils.copyProperties(setMeal, setMealFromDB);
            this.updateById(setMealFromDB);
            //进行菜品关联信息修改
            //先删除原有关系
            QueryWrapper<SetMealDish> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(SetMealDish::getSetMealId, setMeal.getId());
            this.setMealDishService.remove(queryWrapper);
            //添加新的菜品关联
            List<SetMealDish> setMealDishList = setMeal.getDishList();
            if (setMealDishList != null && setMealDishList.size() != 0){
                setMealDishList.forEach(setMealDish -> setMealDish.setSetMealId(setMeal.getId()));
                setMealDishService.saveBatch(setMealDishList);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
