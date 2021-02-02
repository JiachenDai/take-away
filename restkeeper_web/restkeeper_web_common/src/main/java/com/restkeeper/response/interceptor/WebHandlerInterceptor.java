package com.restkeeper.response.interceptor;

import com.restkeeper.tenant.TenantContext;
import com.restkeeper.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
@Component
public class WebHandlerInterceptor implements HandlerInterceptor {
    //在controller调用前进行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取jwt令牌
        String tokenInfo = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(tokenInfo)){
            try{
                Map<String, Object> tokenMap = JWTUtil.decode(tokenInfo);
//                String shopId = (String) tokenMap.get("shopId");

                //将shopId存入上下文
//                RpcContext.getContext().setAttachment("shopId", shopId);
                //将tokenMap存入自定义的容器
                TenantContext.addAttachments(tokenMap);
                return true;
            }catch (Exception e){
                e.printStackTrace();
                log.info("解析令牌失败");
            }

        }
        return true;
    }
}
