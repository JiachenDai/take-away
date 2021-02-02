package com.restkeeper.store.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.sms.SmsObject;
import com.restkeeper.store.mapper.StaffMapper;
import com.restkeeper.store.entity.Staff;
import com.restkeeper.utils.JWTUtil;
import com.restkeeper.utils.MD5CryptUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service(version = "1.0.0",protocol = "dubbo")
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements IStaffService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${sms.operator.signName}")
    private String signName;

    @Value("${sms.operator.templateCode}")
    private String templateCode;

    @Value("{gateway.secret}")
    private String secret;

    @Override
    @Transactional
    public boolean addStaff(Staff staff) {
        String password = staff.getPassword();
        if (StringUtils.isEmpty(password)){
            //密码为空生成密码
            password = RandomStringUtils.random(8);
            System.out.println(password);
        }
        staff.setPassword(Md5Crypt.md5Crypt(password.getBytes()));
        try {
            this.save(staff);
            //发送短信
            sendMessage(staff.getPhone(), staff.getShopId(), password);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public Result login(String shopId, String loginName, String password) {
        Result result = new Result();
        if (StringUtils.isEmpty(loginName)){
            result.setStatus(ResultCode.error);
            result.setDesc("用户名为空");
            return result;
        }
        if (StringUtils.isEmpty(password)){
            result.setStatus(ResultCode.error);
            result.setDesc("密码为空");
            return result;
        }
        if (StringUtils.isEmpty(shopId)){
            result.setStatus(ResultCode.error);
            result.setDesc("商户号为空");
            return result;
        }
        //调用自定义查询
        Staff staff = this.baseMapper.login(shopId, loginName);
        if (staff == null){
            result.setStatus(ResultCode.error);
            result.setDesc("不存在该用户");
            return result;
        }
        //校验密码
        String salts = MD5CryptUtil.getSalts(staff.getPassword());
        if (!Md5Crypt.md5Crypt(password.getBytes(), salts).equals(staff.getPassword())){
            result.setStatus(ResultCode.error);
            result.setDesc("密码错误");
            return result;
        }
        //生成JWT令牌
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("shopId", shopId);
        tokenMap.put("storeId", staff.getStoreId());
        tokenMap.put("loginUserId", staff.getStaffId());
        tokenMap.put("loginUserName", loginName);
        tokenMap.put("userType", SystemCode.USER_TYPE_STAFF);
        String token = null;
        try {
            token = JWTUtil.createJWTByObj(tokenMap, secret);
        } catch (IOException e) {
            e.printStackTrace();
            result.setStatus(ResultCode.error);
            result.setDesc("生成令牌失败");
            return result;
        }
        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setData(staff);
        result.setToken(token);
        return result;
    }


    private void sendMessage(String phone, String shopId, String pwd) {
        SmsObject smsObject = new SmsObject();
        smsObject.setPhoneNumber(phone);
        smsObject.setSignName(signName);
        smsObject.setSignName(templateCode);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("shopId", shopId);
        jsonObject.put("password", pwd);
        smsObject.setTemplateJsonParam(jsonObject.toJSONString());
        rabbitTemplate.convertAndSend(SystemCode.SMS_ACCOUNT_QUEUE, JSON.toJSONString(smsObject));
    }
}
