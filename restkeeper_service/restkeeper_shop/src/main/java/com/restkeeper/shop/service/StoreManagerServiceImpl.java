package com.restkeeper.shop.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.shop.mapper.StoreManagerMapper;
import com.restkeeper.sms.SmsObject;
import com.restkeeper.utils.JWTUtil;
import com.restkeeper.utils.MD5CryptUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import io.netty.util.internal.StringUtil;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(version = "1.0.0",protocol = "dubbo")
@RefreshScope
public class StoreManagerServiceImpl extends ServiceImpl<StoreManagerMapper, StoreManager> implements IStoreManagerService {
    @Autowired
    @Qualifier("storeService")
    private IStoreService storeService;

    @Value("${sms.operator.signName}")
    private String signName;

    @Value("${sms.operator.templateCode}")
    private String templateCode;

    @Value("${gateway.secret}")
    private String secret;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public IPage<StoreManager> queryPageByCriteria(int pageNum, int pageSize, String criteria) {
        IPage<StoreManager> page = new Page<>(pageNum, pageSize);
        QueryWrapper<StoreManager> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(criteria)){
            queryWrapper.lambda().eq(StoreManager::getStoreManagerName, criteria).
                    or().eq(StoreManager::getStoreManagerPhone, criteria);
        }
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public boolean addStoreManager(String name, String phone, List<String> storeIds) {
        boolean flag = true;
        try {
            //新增店长基础信息
            StoreManager storeManager = new StoreManager();
            storeManager.setStoreManagerName(name);
            storeManager.setStoreManagerPhone(phone);
            String pwd = RandomStringUtils.randomNumeric(8);
            System.out.println("密码是： --------------：" + pwd);
            storeManager.setPassword(Md5Crypt.md5Crypt(pwd.getBytes()));
            this.save(storeManager);

            //修改相关store信息，一个门店只有一个门店管理员
            String storeManagerId = storeManager.getStoreManagerId();
            UpdateWrapper<Store> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().in(Store::getStoreId, storeIds).set(Store::getStoreManagerId, storeManagerId);
            flag = storeService.update(updateWrapper);
            if (flag){
                //新增管理员成功，并且门店关联关系创建成功
                //进行短信发送
                sendMessage(phone, storeManager.getShopId(), pwd);
                //到这说明发送短信成功
            }
        }catch (Exception e){
            flag = false;
            throw e;
        }
        return true;
    }

    @Override
    @Transactional
    public boolean updateStoreManager(String storeManagerId, String name, String phone, List<String> storeIds) {
        boolean flag = true;
        try{
            StoreManager storeManager = this.getById(storeManagerId);
            //修改管理员信息
            if (StringUtils.isNotEmpty(name)){
                storeManager.setStoreManagerName(name);
            }
            if (StringUtils.isNotEmpty(phone)){
                storeManager.setStoreManagerPhone(phone);
            }
            this.updateById(storeManager);
            //完成了基础信息修改
            //去除原有管理员与门店的关联关系
            UpdateWrapper<Store> updateWrapperOld = new UpdateWrapper<>();
            updateWrapperOld.lambda().set(Store::getStoreManagerId, null).eq(Store::getStoreManagerId, storeManagerId);
            storeService.update(updateWrapperOld);
            //重建管理员与门店的最新关联关系
            UpdateWrapper<Store> updateWrapperNew = new UpdateWrapper<>();
            updateWrapperNew.lambda().set(Store::getStoreManagerId, storeManagerId).in(Store::getStoreId, storeIds);
            storeService.update(updateWrapperNew);

        }catch (Exception e){
            log.error(e.getMessage());
            flag = false;
        }

        return flag;
    }

    @Override
    @Transactional
    public boolean pauseStoreManager(String storeManagerId) {
        boolean flag = true;
        try {
            if (StringUtils.isNotEmpty(storeManagerId)){
                UpdateWrapper<StoreManager> updateWrapper = new UpdateWrapper<>();
                updateWrapper.set("status", 0).eq("store_manager_id", storeManagerId);
                this.update(updateWrapper);
            }
        } catch (Exception e){
            e.printStackTrace();
            flag = false;
        }

        return flag;
    }

    @Override
    @Transactional
    public boolean deleteStoreManager(String storeManagerId) {
        //完成逻辑删除
        this.removeById(storeManagerId);

        //去除原有的门店管理员关联关系
        UpdateWrapper<Store> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(Store::getStoreManagerId, null).eq(Store::getStoreManagerId, storeManagerId);
        boolean flag = storeService.update(updateWrapper);
        return flag;
    }

    @Override
    @Transactional
    public Result login(String shopId, String phone, String password) {
        Result result = new Result();
        if (StringUtils.isEmpty(shopId)){
            result.setStatus(ResultCode.error);
            result.setDesc("未输入商户id");
            return result;
        }

        if (StringUtils.isEmpty(phone)){
            result.setStatus(ResultCode.error);
            result.setDesc("未输入用户名");
            return result;
        }
        if (StringUtils.isEmpty(password)){
            result.setStatus(ResultCode.error);
            result.setDesc("未输入密码");
            return result;
        }
        QueryWrapper<StoreManager> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StoreManager::getShopId, shopId);
        queryWrapper.lambda().eq(StoreManager::getStoreManagerPhone, phone);
        RpcContext.getContext().setAttachment("shopId", shopId);
        StoreManager storeManager = this.getOne(queryWrapper);
        if (storeManager == null){
            result.setStatus(ResultCode.error);
            result.setDesc("没有该用户");
            return result;
        }
        //获取被关联的门店信息
        List<Store> stores = storeManager.getStores();
        if (stores == null || stores.isEmpty()){
            result.setStatus(ResultCode.error);
            result.setDesc("没有相关联的门店信息");
            return result;
        }
        Store store = stores.get(0);

        //校验密码
        String salts = MD5CryptUtil.getSalts(storeManager.getPassword());
        if (!Md5Crypt.md5Crypt(password.getBytes(), salts).equals(storeManager.getPassword())){
            result.setStatus(ResultCode.error);
            result.setDesc("密码输入错误");
            return result;
        }
        //生成jwt令牌
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("shopId", shopId);
        tokenMap.put("storeId", store.getStoreId());
        tokenMap.put("loginUserId", storeManager.getStoreManagerId());
        tokenMap.put("loginUserName", storeManager.getStoreManagerName());
        tokenMap.put("userType", SystemCode.USER_TYPE_STORE_MANAGER);
        String jwtToken = null;
        try {
            jwtToken = JWTUtil.createJWTByObj(tokenMap, secret);
        } catch (IOException e) {
            e.printStackTrace();
            result.setStatus(ResultCode.error);
            result.setDesc("生成令牌失败");
            return result;
        }

        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setToken(jwtToken);
        result.setData(storeManager);
        return result;
    }


    private void sendMessage(String phone, String shopId, String pwd){
        SmsObject smsObject = new SmsObject();
        smsObject.setPhoneNumber(phone);
        smsObject.setTemplateCode(templateCode);
        smsObject.setSignName(signName);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("shopId", shopId);
        jsonObject.put("password", pwd);
        String jsonString = jsonObject.toJSONString();
        smsObject.setTemplateJsonParam(jsonString);
        rabbitTemplate.convertAndSend(SystemCode.SMS_ACCOUNT_QUEUE, JSON.toJSONString(smsObject));
    }
}
