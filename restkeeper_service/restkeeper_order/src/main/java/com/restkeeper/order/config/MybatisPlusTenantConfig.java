package com.restkeeper.order.config;

import com.baomidou.mybatisplus.core.parser.ISqlParserFilter;
import com.baomidou.mybatisplus.core.parser.SqlParserHelper;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;
import com.google.common.collect.Lists;
import com.restkeeper.tenant.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MybatisPlusTenantConfig {

    //设置多租户字段
    private static final String SYSTEM_TENANT_SHOPID="shop_id";
    private static final String SYSTEM_TENANT_STOREID="store_id";

    //有哪些表会忽略多租户操作
    private static final List<String> INGORE_TENANT_TABLES= Lists.newArrayList("");


    @Bean
    public PaginationInterceptor paginationInterceptor(){

        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();

        //shop_id
        TenantSqlParser tenantSqlParser_shop = new TenantSqlParser().setTenantHandler(new TenantHandler() {
            @Override
            public Expression getTenantId(boolean where) {
                //获取租户字段的值
                //从rpccontext中进行获取
                String shopId = RpcContext.getContext().getAttachment("shopId");
                if (shopId == null || shopId.length() == 0 || "null".equals(shopId)){
                    //从rpccontext中获取失败，再尝试从自定义容器中获取
                    shopId = TenantContext.getShopId();
                    if (shopId == null){
                        throw new RuntimeException("get shopId error");
                    }
                }
                return new StringValue(shopId);
            }

            @Override
            public String getTenantIdColumn() {
                //设置当前的多租户字段
                return SYSTEM_TENANT_SHOPID;
            }

            @Override
            public boolean doTableFilter(String tableName) {
                return INGORE_TENANT_TABLES.stream().anyMatch((e)->e.equalsIgnoreCase(tableName));
            }
        });

        //store_id
        TenantSqlParser tenantSqlParser_store = new TenantSqlParser().setTenantHandler(new TenantHandler() {
            @Override
            public Expression getTenantId(boolean where) {
                //获取租户字段的值
                //从rpccontext中进行获取
                String storeId = RpcContext.getContext().getAttachment("storeId");
                if (storeId == null || storeId.length() == 0 || "null".equals(storeId)){
                    //从rpccontext中获取失败，再尝试从自定义容器中获取
                    storeId = TenantContext.getStorId();
                    if (storeId == null){
                        throw new RuntimeException("get storeId error");
                    }
                }
                return new StringValue(storeId);
            }

            @Override
            public String getTenantIdColumn() {
                //设置当前的多租户字段
                return SYSTEM_TENANT_STOREID;
            }

            @Override
            public boolean doTableFilter(String tableName) {
                return INGORE_TENANT_TABLES.stream().anyMatch((e)->e.equalsIgnoreCase(tableName));
            }
        });


        paginationInterceptor.setSqlParserList(Lists.newArrayList(tenantSqlParser_shop,tenantSqlParser_store));

        //自定义忽略多租户操作方法
        paginationInterceptor.setSqlParserFilter(new ISqlParserFilter() {
            @Override
            public boolean doFilter(MetaObject metaObject) {
                MappedStatement ms = SqlParserHelper.getMappedStatement(metaObject);
                // 过滤自定义查询此时无租户信息约束
                if ("com.restkeeper.store.mapper.StaffMapper.login".equals(ms.getId())) {
                    return true;
                }
                return false;
            }
        });
        return paginationInterceptor;
    }
}
