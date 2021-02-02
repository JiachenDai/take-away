package com.restkeeper.controller.store;

import com.restkeeper.store.entity.Staff;
import com.restkeeper.store.service.IStaffService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Api("管家端员工管理接口")
@RequestMapping("/staff")
public class StaffController {

    @Reference(version = "1.0.0", check=false)
    private IStaffService staffService;

    /**
     * 新增员工
     */
    @ApiOperation(value = "新增员工")
    @PostMapping(value = "/add")
    public boolean add(@RequestBody Staff staff){
        return staffService.addStaff(staff);
    }
}
