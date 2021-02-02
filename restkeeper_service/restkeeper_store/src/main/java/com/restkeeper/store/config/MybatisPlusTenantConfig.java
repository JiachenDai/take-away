package com.restkeeper.store.config;

import com.baomidou.mybatisplus.core.parser.ISqlParserFilter;
import com.baomidou.mybatisplus.core.parser.SqlParserHelper;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;
import com.google.common.collect.Lists;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.commons.fileupload.util.LimitedInputStream;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MybatisPlusTenantConfig {

    //设置操作的多租户字段
    private static final String SYSTEM_TENANT_SHOPID = "shop_id";

    private static final String SYSTEM_TENANT_STOREID = "store_id";

    //设置不需要多租户操作的表
    private static final List<String> IGNORE_TENANT_TABLES = Lists.newArrayList("");

    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        TenantSqlParser tenantSqlParserShop = new TenantSqlParser().setTenantHandler(new TenantHandler() {
            @Override
            public Expression getTenantId(boolean where) {
                //获取租户字段
                String shopId = RpcContext.getContext().getAttachment("shopId");
                if (shopId == null){
                    throw new RuntimeException("get shopId error");
                }
                return new StringValue(shopId);
            }

            @Override
            public String getTenantIdColumn() {
                //设置多租户字段
                return SYSTEM_TENANT_SHOPID;
            }

            @Override
            public boolean doTableFilter(String tableName) {
                return IGNORE_TENANT_TABLES.stream().anyMatch((e) -> e.equalsIgnoreCase(tableName));
            }
        });

        TenantSqlParser tenantSqlParserStore = new TenantSqlParser().setTenantHandler(new TenantHandler() {
            @Override
            public Expression getTenantId(boolean where) {
                //获取租户字段
                String storeId = RpcContext.getContext().getAttachment("storeId");
                if (storeId == null){
                    throw new RuntimeException("get storeId error");
                }
                return new StringValue(storeId);
            }

            @Override
            public String getTenantIdColumn() {
                //设置多租户字段
                return SYSTEM_TENANT_STOREID;
            }

            @Override
            public boolean doTableFilter(String tableName) {
                return IGNORE_TENANT_TABLES.stream().anyMatch((e) -> e.equalsIgnoreCase(tableName));
            }
        });
        paginationInterceptor.setSqlParserList(Lists.newArrayList(tenantSqlParserShop, tenantSqlParserStore));
        //自定义忽略多租户方法
        paginationInterceptor.setSqlParserFilter(new ISqlParserFilter() {
            @Override
            public boolean doFilter(MetaObject metaObject) {
                MappedStatement ms = SqlParserHelper.getMappedStatement(metaObject);
                //过滤到自定义查询，此时无租户信息约束
                if ("com.restkeeper.store.mapper.StaffMapper.login".equals(ms.getId())){
                    return true;
                }
                return false;
            }
        });
        return paginationInterceptor;
    }
}
