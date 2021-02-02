package com.restkeeper.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restkeeper.redis.MybatisRedisCache;
import com.restkeeper.store.entity.DishFlavor;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
@CacheNamespace(implementation= MybatisRedisCache.class,eviction=MybatisRedisCache.class)
public interface DishFlavorMapper extends BaseMapper<DishFlavor>{

    //根据dishId来查询口味列表
    @Select("select * from t_dish_flavor where dish_id = #{dishId} order by last_update_time desc")
    List<DishFlavor> selectFlavors(@Param("dishId") String dishId);
}
