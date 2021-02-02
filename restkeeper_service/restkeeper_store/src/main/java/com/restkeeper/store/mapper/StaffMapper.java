package com.restkeeper.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restkeeper.redis.MybatisRedisCache;
import com.restkeeper.store.entity.Staff;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
@CacheNamespace(implementation= MybatisRedisCache.class,eviction=MybatisRedisCache.class)
public interface StaffMapper extends BaseMapper<Staff> {

    @Select("select * from t_staff where shop_id = #{shopId} and staff_name = #{loginName}")
    Staff login(@Param("shopId") String shopId, @Param("loginName") String loginName);
}
