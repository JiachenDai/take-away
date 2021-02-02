package com.restkeeper.service;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.fastjson.JSON;
import com.restkeeper.entity.DishEs;
import com.restkeeper.entity.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.nlpcn.es4sql.domain.Where;
import org.nlpcn.es4sql.parse.ElasticSqlExprParser;
import org.nlpcn.es4sql.parse.SqlParser;
import org.nlpcn.es4sql.parse.WhereParser;
import org.nlpcn.es4sql.query.maker.QueryMaker;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service(version = "1.0.0",protocol = "dubbo")
public class DishSearchServiceImpl implements IDishSearchService{

    @Value("${es.host}")
    private String host;

    @Value("${es.port}")
    private String port;

    @Override
    public SearchResult<DishEs> searchAllByCode(String code, int type, int pageNum, int pageSize) {
        String shopId = RpcContext.getContext().getAttachment("shopId");
        String storeId = RpcContext.getContext().getAttachment("storeId");
        if (StringUtils.isEmpty(shopId)){
            throw new RuntimeException("商户号为空");
        }
        if (StringUtils.isEmpty(storeId)){
            throw new RuntimeException("门店号为空");
        }
        //数据查询
        //使用es7的新特性，使用sql语句进行查询

        return this.queryIndexContent("dish", "code like '%"+code+"%' " +
                "and type = '"+type+"' and is_deleted = 0 " +
                "and shop_id = '"+shopId+"' " +
                "and store_id = '"+storeId+"' order by last_update_time desc", pageNum, pageSize);
    }

    private SearchResult<DishEs> queryIndexContent(String indexName, String condition, int pageNum, int pageSize) {
        System.out.println(port);
        //构建查询
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, Integer.parseInt(port), "http")));
        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //构建查询条件
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        //是否跟踪查询的总命中数
        searchSourceBuilder.trackTotalHits(true);
        BoolQueryBuilder boolQueryBuilder = this.createQueryBuilder(indexName, condition);

        searchSourceBuilder.query(boolQueryBuilder);
        request.source(searchSourceBuilder);
        System.out.println(searchSourceBuilder);
        //获取查询结果并操作
        SearchResponse response = null;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        List<DishEs> listData = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> datas = searchHit.getSourceAsMap();
            String jsonString = JSON.toJSONString(datas);
            DishEs dishEs = JSON.parseObject(jsonString, DishEs.class);
            listData.add(dishEs);
        }
        //关闭客户端连接
        try {
            client.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        SearchResult<DishEs> result = new SearchResult<>();
        result.setRecords(listData);
        result.setTotal(response.getHits().getTotalHits().value);
        return result;
    }

    private BoolQueryBuilder createQueryBuilder(String indexName, String condition) {
        BoolQueryBuilder boolQuery = null;

        try {
            SqlParser sqlParser = new SqlParser();
            String sql = "select * from " + indexName;
            String whereTemp = "";
            if (!Strings.isNullOrEmpty(condition)){
                whereTemp = " where 1 = 1 and " + condition;
            }

            SQLQueryExpr sqlQueryExpr = (SQLQueryExpr) this.toSqlExpr(sql + whereTemp);
            MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) sqlQueryExpr.getSubQuery().getQuery();
            WhereParser whereParser = new WhereParser(sqlParser, query);
            Where where = whereParser.findWhere();
            if (where != null){
                boolQuery = QueryMaker.explan(where);
            }
        } catch (Exception e){
            log.error(e.getMessage());
        }


        return boolQuery;
    }

    private SQLExpr toSqlExpr(String sql) {
        SQLExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();
        if (parser.getLexer().token() != Token.EOF){
            throw new ParseException("当前sql有问题");
        }
        return expr;
    }

    @Override
    public SearchResult<DishEs> searchDishByCode(String code, int pageNum, int pageSize) {
        String shopId = RpcContext.getContext().getAttachment("shopId");
        String storeId = RpcContext.getContext().getAttachment("storeId");
        if (StringUtils.isEmpty(shopId)){
            throw new RuntimeException("商户号为空");
        }
        if (StringUtils.isEmpty(storeId)){
            throw new RuntimeException("门店号为空");
        }
        //数据查询
        //使用es7的新特性，使用sql语句进行查询

        return this.queryIndexContent("dish", "code like '%"+code+"%' " +
                "and is_deleted = 0 " +
                "and shop_id = '"+shopId+"' " +
                "and store_id = '"+storeId+"' order by last_update_time desc", pageNum, pageSize);
    }

    @Override
    public SearchResult<DishEs> searchDishByName(String name, int type, int pageNum, int pageSize) {
        String shopId = RpcContext.getContext().getAttachment("shopId");
        String storeId = RpcContext.getContext().getAttachment("storeId");
        if (StringUtils.isEmpty(shopId)){
            throw new RuntimeException("商户号为空");
        }
        if (StringUtils.isEmpty(storeId)){
            throw new RuntimeException("门店号为空");
        }
        //数据查询
        //使用es7的新特性，使用sql语句进行查询

        return this.queryIndexContent("dish", "name like '%"+name+"%' " +
                "and type = '"+type+"' and is_deleted = 0 " +
                "and shop_id = '"+shopId+"' " +
                "and store_id = '"+storeId+"' order by last_update_time desc", pageNum, pageSize);
    }
}
