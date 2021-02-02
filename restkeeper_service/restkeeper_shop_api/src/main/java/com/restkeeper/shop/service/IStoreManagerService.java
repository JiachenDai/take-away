package com.restkeeper.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.utils.Result;

import java.util.List;

public interface IStoreManagerService extends IService<StoreManager> {

    /*
    店长列表查询方法，既需要分页功能，又需要根据店长联系方式和姓名进行等值查询
     */
    IPage<StoreManager> queryPageByCriteria(int pageNum, int pageSize, String criteria);

    //增加门店管理员
    boolean addStoreManager(String name, String phone, List<String> storeIds);

    /*
    修改门店管理员
     */
    boolean updateStoreManager(String storeManagerId, String name, String phone, List<String> storeIds);

    /*
    门店管理员停用
     */
    boolean pauseStoreManager(String storeManagerId);

    /*
    门店管理员停用
     */
    boolean deleteStoreManager(String storeManagerId);

    //门店管理员的登录接口
    Result login(String shopId, String phone, String password);


}
