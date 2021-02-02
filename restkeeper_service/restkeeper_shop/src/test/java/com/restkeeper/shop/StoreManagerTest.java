package com.restkeeper.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.shop.service.IStoreManagerService;
import com.restkeeper.shop.service.IStoreService;
import com.restkeeper.tenant.TenantContext;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class StoreManagerTest {

    @Reference(version = "1.0.0", check = false)
    private IStoreManagerService storeManagerService;

    @Before
    public void init(){
//        RpcContext.getContext().setAttachment("shopId", "test2");
        Map<String, Object> map = new HashMap<>();
        map.put("shopId", "33912009");
        TenantContext.addAttachments(map);
    }

    @Test
    public void queryPageByCriteria(){
        IPage<StoreManager> page = storeManagerService.queryPageByCriteria(1, 10, "demo");
        List<StoreManager> records = page.getRecords();
        System.out.println(records);
    }

    @Test
    @Rollback(false)
    public void add(){
        List<String> storeIds = new ArrayList<>();
        storeIds.add("1345845122902769665");
        storeManagerService.addStoreManager("lisi","15666666666",storeIds);
    }

    @Test
    @Rollback(false)
    public void updateStoreManager(){
        List<String> storeIds = new ArrayList<>();
        storeIds.add("1345844866257473538");
        storeIds.add("1345845122902769665");
        storeManagerService.updateStoreManager("1346421370783784961","wangwu","18810973345",storeIds);
    }

    @Test
    @Rollback(false)
    public void deleteStoreManager(){
        storeManagerService.deleteStoreManager("1346421370783784961");
    }


    @Test
    @Rollback(false)
    public void pauseStoreManager(){
        storeManagerService.pauseStoreManager("1346421370783784961");
    }
}
