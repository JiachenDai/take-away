package com.restkeeper.controller.store;

import com.restkeeper.store.entity.Remark;
import com.restkeeper.store.service.IRemarkService;
import com.restkeeper.vo.store.RemarkVO;
import com.restkeeper.vo.store.SettingsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@Api(tags = {"门店备注管理"})
@Slf4j
@RequestMapping("/settings")
public class SettingsController {

    @Reference(version = "1.0.0", check = false)
    private IRemarkService remarkService;

    @ApiOperation("获取门店设置信息")
    @GetMapping("/getSysSetting")
    public SettingsVO getSysSettings(){
        List<Remark> remarks = remarkService.getRemarks();
        List<RemarkVO> remarkVOS = new ArrayList<>();
        for (Remark remark : remarks) {
            RemarkVO remarkVO = new RemarkVO();
            remarkVO.setRemarkName(remark.getRemarkName());
            String remarkValue = remark.getRemarkValue();
            String[] strings = remarkValue.substring(remarkValue.indexOf("[") + 1, remarkValue.indexOf("]")).split(",");
            List<String> list = new ArrayList<>();
            for (String string : strings) {
                list.add(string);
            }
            remarkVO.setRemarkValue(list);
            remarkVOS.add(remarkVO);
        }
        SettingsVO settingsVO = new SettingsVO();
        settingsVO.setRemarks(remarkVOS);
        return settingsVO;
    }

    @ApiOperation("修改门店备注")
    @PutMapping("/update")
    public boolean update(@RequestBody SettingsVO settingsVO){
        List<RemarkVO> remarks = settingsVO.getRemarks();
        List<Remark> remarkList = new ArrayList<>();
        for (RemarkVO remarkVO : remarks) {
            Remark remark = new Remark();
            remark.setRemarkName(remarkVO.getRemarkName());
            remark.setRemarkValue(remarkVO.getRemarkValue().toString());
            remarkList.add(remark);
        }
        return remarkService.updateRemarks(remarkList);
    }
}
