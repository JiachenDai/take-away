package com.restkeeper.controller.store;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditCompanyUser;
import com.restkeeper.store.entity.CreditLogs;
import com.restkeeper.store.entity.CreditRepayment;
import com.restkeeper.store.service.ICreditLogService;
import com.restkeeper.store.service.ICreditRepaymentService;
import com.restkeeper.store.service.ICreditService;
import com.restkeeper.utils.BeanListUtils;
import com.restkeeper.vo.store.CreditCompanyUserVO;
import com.restkeeper.vo.store.CreditLogExcelVO;
import com.restkeeper.vo.store.CreditRepaymentVO;
import com.restkeeper.vo.store.CreditVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.core.Local;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Api(tags = { "挂账管理" })
@RestController
@RequestMapping("/credit")
public class CreditController {

    @Reference(version = "1.0.0", check = false)
    private ICreditService creditService;

    @Reference(version = "1.0.0", check = false)
    private ICreditLogService creditLogService;

    @Reference(version = "1.0.0", check=false)
    private ICreditRepaymentService creditRepaymentService;

    @PostMapping("/add")
    @ApiOperation("新增挂账")
    public boolean add(@RequestBody CreditVO creditVO){
        //creditVO转换为Credit
        Credit credit = new Credit();
        BeanUtils.copyProperties(creditVO, credit, "users");
        //设置users
        List<CreditCompanyUser> creditCompanyUsers = new ArrayList<>();
        List<CreditCompanyUserVO> creditVOUsers = creditVO.getUsers();
        if (creditVOUsers != null && creditVOUsers.size() != 0){
            for (CreditCompanyUserVO creditVOUser : creditVOUsers) {
                CreditCompanyUser creditCompanyUser = new CreditCompanyUser();
                BeanUtils.copyProperties(creditVOUser, creditCompanyUser);
                creditCompanyUsers.add(creditCompanyUser);
            }
            return creditService.add(credit, creditCompanyUsers);
        }
        return creditService.add(credit, null);
    }

    @ApiOperation("挂账管理列表")
    @GetMapping("/pageList/{page}/{pageSize}")
    public PageVO<CreditVO> pageList(@PathVariable("page") int page,
                                     @PathVariable("pageSize") int pageSize,
                                     @RequestParam(value = "name", defaultValue = "", required = false) String name){
        IPage<Credit> queryPage = creditService.queryPage(page, pageSize, name);
        List<CreditVO> voList = null;
        try {
            voList = BeanListUtils.copy(queryPage.getRecords(), CreditVO.class);
        } catch (Exception e) {
            throw new RuntimeException("集合转换出错");
        }
        return new PageVO<>(queryPage, voList);
    }

    @ApiOperation(value = "根据id获取挂账详情")
    @GetMapping("/{id}")
    public CreditVO getCredit(@PathVariable String id){
        CreditVO creditVO =new CreditVO();
        Credit credit = creditService.queryById(id);
        BeanUtils.copyProperties(credit,creditVO);
        return creditVO;
    }

    @PutMapping("/update/{id}")
    @ApiOperation("修改挂账")
    public boolean updateCredit(@PathVariable("id") String id, @RequestBody CreditVO creditVO){
        //CreditVO->转化成 Credit
        Credit credit = creditService.queryById(id);
        BeanUtils.copyProperties(creditVO, credit, "users");
        if (creditVO.getUsers() != null && !creditVO.getUsers().isEmpty()){
            //List<CreditCompanyUserVO> 转换成 List<CreditCompanyUser>
            List<CreditCompanyUser> companyUsers = new ArrayList<>();
            creditVO.getUsers().forEach(d -> {
                CreditCompanyUser creditCompanyUser = new CreditCompanyUser();
                BeanUtils.copyProperties(d, creditCompanyUser);
                companyUsers.add(creditCompanyUser);
            });
            return creditService.updateInfo(credit, companyUsers);
        }
        return creditService.updateInfo(credit, null);
    }


    /**
     * 挂账明细列表
     * @param creditId
     * @param page
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "挂账订单明细列表")
    @GetMapping("/creditLog/{page}/{pageSize}/{creditId}")
    public PageVO<CreditLogs> getCreditLogPageList(@RequestParam(value = "creditId") String creditId,
                                                   @PathVariable int page,
                                                   @PathVariable int pageSize){
        return new PageVO<CreditLogs>(creditLogService.queryPage(page,pageSize,creditId));
    }

    //excel文件的生成与导出
    @GetMapping("/export/creditId/{creditId}/start/{start}/end/{end}")
    public void export(HttpServletResponse response, @PathVariable String creditId, @PathVariable String start, @PathVariable String end) throws IOException {
        //时间格式化
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        if (endTime.isBefore(startTime)){
            throw new RuntimeException("结束时间不能比开始时间小");
        }
        //设置具体的数据信息
        List<CreditLogs> list = creditLogService.list(creditId, startTime, endTime);
        //将CreditLogs的集合转换成VO集合
        List<CreditLogExcelVO> voList = new ArrayList<>();
        for (CreditLogs creditLogs : list) {
            CreditLogExcelVO creditLogExcelVO = new CreditLogExcelVO();
            creditLogExcelVO.setCreditAmount(creditLogs.getCreditAmount());
            creditLogExcelVO.setDateTime(Date.from(creditLogs.getLastUpdateTime().atZone(ZoneId.systemDefault()).toInstant()));
            creditLogExcelVO.setOrderAmount(creditLogs.getOrderAmount());
            creditLogExcelVO.setRevenueAmount(creditLogs.getReceivedAmount());
            creditLogExcelVO.setUserName(creditLogs.getUserName());
            creditLogExcelVO.setOrderId(creditLogs.getOrderId());
            if (creditLogs.getType() == SystemCode.CREDIT_TYPE_COMPANY){
                creditLogExcelVO.setCreditType("企业");
            } else {
                creditLogExcelVO.setCreditType("个人");
            }
            voList.add(creditLogExcelVO);
        }
        //设置头信息，完成下载操作
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("demo", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename="+fileName+".xlsx");
        //向excel中写入数据
        EasyExcel.write(response.getOutputStream(), CreditLogExcelVO.class).sheet("模板").doWrite(voList);
    }

    @ApiOperation(value = "还款")
    @PostMapping("/repayment")
    public boolean repayment(@RequestBody CreditRepaymentVO creditRepaymentVo){
        CreditRepayment creditRepayment =new CreditRepayment();
        BeanUtils.copyProperties(creditRepaymentVo,creditRepayment);
        return creditRepaymentService.repayment(creditRepayment);
    }
}
