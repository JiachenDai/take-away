package com.restkeeper.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.operator.entity.OperatorUser;
import com.restkeeper.operator.mapper.OperatorUserMapper;
import com.restkeeper.utils.JWTUtil;
import com.restkeeper.utils.MD5CryptUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//@Service("operatorUserService")
@Service(version = "1.0.0",protocol = "dubbo")
/**
 * dubbo中支持的协议
 * dubbo 默认
 * rmi
 * hessian
 * http
 * webservice
 * thrift
 * memcached
 * redis
 */
@RefreshScope//配置中心文件内容改变后，会动态获取
public class OperatorUserServiceImpl extends ServiceImpl<OperatorUserMapper, OperatorUser> implements IOperatorUserService{
    @Value("${gateway.secret}")
    private String secret;

    @Override
    public Result login(String loginName, String loginPass) {
        Result result = new Result();
        //参数校验
        if(StringUtils.isEmpty(loginName)){
            result.setStatus(ResultCode.error);
            result.setDesc("用户名不存在");
            return result;
        }
        if(StringUtils.isEmpty(loginPass)){
            result.setStatus(ResultCode.error);
            result.setDesc("密码不存在");
            return result;
        }

        //进行查询用户是否存在
        QueryWrapper<OperatorUser> queryWapper = new QueryWrapper<>();
        queryWapper.eq("loginname", loginName);
        OperatorUser operatorUser = this.getOne(queryWapper);
        //如果该用户不存在
        if (operatorUser == null){
            result.setStatus(ResultCode.error);
            result.setDesc("该用户不存在");
            return result;
        }
        //进行密码校验
        //首先需要通过MD5Util来获取数据库中加密后的密码的salt值
        String salts = MD5CryptUtil.getSalts(operatorUser.getLoginpass());
        if (!Md5Crypt.md5Crypt(loginPass.getBytes(), salts).equals(operatorUser.getLoginpass())){
            //密码不正确
            result.setStatus(ResultCode.error);
            result.setDesc("密码不正确");
            return result;
        }

        //生成jwt令牌
        //准备数据
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("loginName", operatorUser.getLoginname());
        String jwtToken;
        //生成令牌
        try {
            jwtToken = JWTUtil.createJWTByObj(tokenInfo, secret);
        } catch (IOException e) {
            e.printStackTrace();
            //生成令牌失败
            result.setStatus(ResultCode.error);
            result.setDesc("生成令牌失败");
            return result;
        }

        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setData(operatorUser);
        result.setToken(jwtToken);
        return result;
    }
}
