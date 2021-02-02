package com.restkeeper.service;

import com.restkeeper.entity.DishEs;
import com.restkeeper.entity.SearchResult;

public interface IDishSearchService {
	//根据商品码和类型查询信息
    SearchResult<DishEs> searchAllByCode(String code, int type, int pageNum, int pageSize);

    //根据商品码查询信息
    SearchResult<DishEs> searchDishByCode(String code, int pageNum, int pageSize);

    //根据菜品名称查询信息
    SearchResult<DishEs> searchDishByName(String name, int type, int pageNum, int pageSize);
}
