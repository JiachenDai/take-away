package com.restkeeper.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.mapper.StoreMapper;
import com.restkeeper.utils.BeanListUtils;
import com.restkeeper.utils.JWTUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service("storeService")
@Service(version = "1.0.0",protocol = "dubbo")
@RefreshScope
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements IStoreService {

    @Value("${gateway.secret}")
    private String secret;


    @Override
    public IPage<Store> queryPageByName(int pageNum, int pageSize, String name) {
        Page<Store> page = new Page<>(pageNum, pageSize);
        QueryWrapper queryWrapper = new QueryWrapper();
        if (StringUtils.isNotEmpty(name)){
            queryWrapper.like("store_name", name);
        }
        return this.page(page, queryWrapper);
    }

    @Override
    public List<String> getAllProvince() {
        return this.getBaseMapper().getAllProvince();
    }

    @Override
    public List<StoreDTO> getStoreByProvince(String province) {
        QueryWrapper<Store> queryWrapper = new QueryWrapper<>();
        //门店状态为营业中
        queryWrapper.lambda().eq(Store::getStatus, 1);
        if (StringUtils.isNotEmpty(province) && !province.equals("all")){
            //选择了省份并且省份不为all
            queryWrapper.lambda().eq(Store::getProvince, province);
        }
        List<Store> list = this.list(queryWrapper);
        //处理结果
        List<StoreDTO> dtoList;
        try {
            dtoList = BeanListUtils.copy(list, StoreDTO.class);
            return dtoList;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("集合转换失败");
        }
        return new ArrayList<StoreDTO>();
    }

    @Override
    public List<StoreDTO> getStoreListByManagerId() {
        QueryWrapper<Store> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(Store::getStoreManagerId, RpcContext.getContext().getAttachment("loginUserId"))
                .eq(Store::getStatus, 1);
        List<Store> list = this.list(queryWrapper);
        //处理结果
        List<StoreDTO> dtoList;
        try {
            dtoList = BeanListUtils.copy(list, StoreDTO.class);
            return dtoList;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("集合转换失败");
        }
        return new ArrayList<StoreDTO>();
    }

    @Override
    public Result switchStore(String storeId) {
        //要做的是重新生成令牌
        Result result = new Result();
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("shopId", RpcContext.getContext().getAttachment("shopId"));
        tokenMap.put("storeId", storeId);
        tokenMap.put("loginUserId", RpcContext.getContext().getAttachment("loginUserId"));
        tokenMap.put("loginUserName", RpcContext.getContext().getAttachment("loginUserName"));
        tokenMap.put("userType", SystemCode.USER_TYPE_STORE_MANAGER);
        String token = "";
        try {
            token = JWTUtil.createJWTByObj(tokenMap, secret);
        } catch (IOException e) {
            e.printStackTrace();
            result.setStatus(ResultCode.error);
            result.setDesc("生成令牌出错");
            return result;
        }
        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setToken(token);
        result.setData(storeId);
        return result;
    }


}
