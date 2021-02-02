package com.restkeeper.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restkeeper.store.entity.SetMealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper extends BaseMapper<SetMealDish>{

    //根据套餐的id来查询关联信息
    @Select("select * from t_setmeal_dish where setmeal_id = #{setMealId}")
    List<SetMealDish> getSetMealListBySetMealId(@Param("setMealId") String setMealId);
}
