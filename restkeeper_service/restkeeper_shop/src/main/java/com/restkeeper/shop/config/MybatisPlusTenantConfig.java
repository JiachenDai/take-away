package com.restkeeper.shop.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;
import com.google.common.collect.Lists;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MybatisPlusTenantConfig {
    //定义当前的多租户标识字段
    private static final String SYSTEM_TENANT_ID = "shop_id";
    
    //定义当前有哪些表要忽略多租户的操作
    private static final List<String> IGNORE_TENANT_TABLES = Lists.newArrayList("");

    @Bean
    public PaginationInterceptor paginationInterceptor(){
        System.out.println("---------------paginationInterceptor begin---------------");
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        //创建多租户sql解析器，起到sql解析、处理、拦截的作用，增加租户处理的回调
        TenantSqlParser tenantSqlParser = new TenantSqlParser().setTenantHandler(new TenantHandler() {
            //设置租户id
            @Override
            public Expression getTenantId(boolean where) {
//                String shopId = "test";
                //底层是从threadLocal中获取了值
                String shopId = RpcContext.getContext().getAttachment("shopId");
                if (shopId == null){
                    throw new RuntimeException("get tenant Id error");
                }
                return new StringValue(shopId);
            }

            //设置租户id对应的表字段
            @Override
            public String getTenantIdColumn() {
                return SYSTEM_TENANT_ID;
            }

            //表级过滤器(哪些表不做多租户操作)
            @Override
            public boolean doTableFilter(String tableName) {
                return IGNORE_TENANT_TABLES.stream().anyMatch((e) -> e.equalsIgnoreCase(tableName));
            }
        });
        //添加自己的sql解析器
        paginationInterceptor.setSqlParserList(Lists.newArrayList(tenantSqlParser));
        return paginationInterceptor;
    }
}
