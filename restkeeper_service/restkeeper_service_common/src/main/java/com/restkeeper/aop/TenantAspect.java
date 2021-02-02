package com.restkeeper.aop;

import com.restkeeper.tenant.TenantContext;
import org.apache.dubbo.rpc.RpcContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantAspect {

    @Pointcut("@annotation(com.restkeeper.aop.TenantAnnotation)")
    public void tenantAnnotation(){}

    @Before("tenantAnnotation()")
    public void doBeforeAdvice(JoinPoint joinPoint){
        TenantContext.addAttachment("storeId", RpcContext.getContext().getAttachment("storeId"));
        TenantContext.addAttachment("shopId", RpcContext.getContext().getAttachment("shopId"));
    }

    @After("tenantAnnotation()")
    public void doAfterAdvice(JoinPoint joinPoint){
        TenantContext.clear();
    }
}
