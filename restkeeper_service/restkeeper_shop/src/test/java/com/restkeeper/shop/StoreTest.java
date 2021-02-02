package com.restkeeper.shop;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.service.IStoreService;
import com.restkeeper.tenant.TenantContext;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.rpc.RpcContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class StoreTest {
    @Reference(version = "1.0.0", check = false)
    private IStoreService storeService;

    @Before
    public void init(){
//        RpcContext.getContext().setAttachment("shopId", "test2");
        Map<String, Object> map = new HashMap<>();
        map.put("shopId", "test2");
        TenantContext.addAttachments(map);
    }

    @Test
    @Rollback(value = false)
    public void saveTest(){
        Store store = new Store();
        store.setBrandId("test");
        store.setStoreName("测试");
        store.setProvince("北京");
        store.setCity("昌平区");
        store.setArea("sadas");
        store.setAddress("basda");
        storeService.save(store);
    }

    @Test
    public void queryTest(){
        for (int i = 0; i < 3; i++) {
            Store store = storeService.getById("1345831561614745602");
            System.out.println(store
            );
        }
    }

    @Test
    public void testStopStore(){
        Store store= storeService.getById("1345845122902769665");
        store.setStatus(0);
        storeService.updateById(store);
    }

    @Test
    public void listAllProvince(){
        List<String> allProvince = storeService.getAllProvince();
        System.out.println(allProvince);
    }

    @Test
    public void getStoreByProvince(){
        List<StoreDTO> storeDTO = storeService.getStoreByProvince("all");
        System.out.println(storeDTO);
    }

}
