package com.restkeeper.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.restkeeper.shop.entity.Brand;
import com.restkeeper.shop.service.IBrandService;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.rpc.RpcContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BrandTest {

    @Reference(version = "1.0.0",check = false)
    private IBrandService brandService;

    @Before
    public void init(){
        RpcContext.getContext().setAttachment("shopId", "test2");
    }


    @Test
    public void queryPage(){
        IPage<Brand> rsult = brandService.queryPage(1, 100);
        List<Brand> records = rsult.getRecords();
        for (Brand record : records) {
            System.out.println(record.getBrandName() + ":" + record.getInfo());
        }
    }

    @Test
    public void getBrandList(){
        List<Map<String, Object>> brandList = brandService.getBrandList();
        System.out.println(brandList);
    }

}
