package com.restkeeper.dubbo;

import com.restkeeper.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Map;

@Activate //开启dubbo扩展
@Slf4j
public class DubboConsumerContextFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
//        log.info("shopId------------------" + RpcContext.getContext().getAttachment("shopId"));
//        log.info("ThreadName--------" + Thread.currentThread().getName());
        //从自定义的上下文对象中获取到令牌相关的信息，放入上下文对象
        RpcContext.getContext().setAttachment("shopId", TenantContext.getShopId());
        RpcContext.getContext().setAttachment("loginUserId", TenantContext.getLoginUserId());
        RpcContext.getContext().setAttachment("loginUserName", TenantContext.getLoginUserName());
        RpcContext.getContext().setAttachment("storeId", TenantContext.getStorId());
        return invoker.invoke(invocation);
    }
}
