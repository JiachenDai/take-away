package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.store.entity.DishCategory;
import com.restkeeper.store.mapper.DishCategoryMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service(version = "1.0.0", protocol = "dubbo")
@org.springframework.stereotype.Service("dishCategoryService")
public class DishCategoryServiceImpl extends ServiceImpl<DishCategoryMapper, DishCategory> implements IDishCategoryService{

    @Override
    @Transactional
    public boolean add(String name, int type) {
        //判断当前分类名称在表中是否存在
        checkNameExist(name);
        //没有异常即可进行数据存入

        DishCategory dishCategory = new DishCategory();
        if (StringUtils.isNotEmpty(name)){
            dishCategory.setName(name);
        }
        dishCategory.setType(type);
        dishCategory.setTorder(0);
        return this.save(dishCategory);
    }

    @Override
    @Transactional
    public boolean update(String id, String name) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(name)){
            return false;
        }
        checkNameExist(name);
        UpdateWrapper<DishCategory> uw = new UpdateWrapper<>();
        uw.lambda()
                .eq(DishCategory::getCategoryId,id)
                .set(DishCategory::getName,name);
        return this.update(uw);
    }

    @Override
    public IPage<DishCategory> queryPage(int pageNum, int pageSize) {
        IPage<DishCategory> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DishCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(DishCategory::getLastUpdateTime);
        return this.page(page, queryWrapper);
    }

    @Override
    public List<Map<String, Object>> findCategoryList(Integer type) {
        QueryWrapper<DishCategory> queryWrapper = new QueryWrapper<>();
        if (type != null){
            queryWrapper.lambda().eq(DishCategory::getType, type);
        }
        queryWrapper.lambda().select(DishCategory::getCategoryId, DishCategory::getName);
        return this.listMaps(queryWrapper);
    }

    private void checkNameExist(String name) {
        QueryWrapper<DishCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(DishCategory::getCategoryId).eq(DishCategory::getName, name);
        Integer count = this.getBaseMapper().selectCount(queryWrapper);
        if (count > 0){
            throw new RuntimeException("该分类已经存在");
        }
    }
}
