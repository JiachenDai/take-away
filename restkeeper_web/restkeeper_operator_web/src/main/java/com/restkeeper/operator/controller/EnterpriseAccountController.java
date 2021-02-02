package com.restkeeper.operator.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.operator.entity.OperatorUser;
import com.restkeeper.operator.service.IEnterpriseAccountService;
import com.restkeeper.operator.vo.AddEnterpriseAccountVO;
import com.restkeeper.operator.vo.UpdateEnterpriseAccountVO;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.utils.AccountStatus;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 企业账户管理接口
 */
@RestController
@RefreshScope //配置中心的自动刷新
@Slf4j
@Api(tags = {"企业账户管理接口"})
@RequestMapping("/enterprise")
public class EnterpriseAccountController {

    @Reference(version = "1.0.0", check = false)
    private IEnterpriseAccountService enterpriseAccountService;

    @ApiOperation("查询企业账号列表")
    @GetMapping("/pageList/{page}/{pageSize}")
    public PageVO<EnterpriseAccount> findListByPage(@PathVariable("page") int pageNum, @PathVariable("pageSize") int pageSize,
                                                    @RequestParam(value = "enterpriseName", required = false) String enterpriseName){
        return new PageVO<>(enterpriseAccountService.queryPageByName(pageNum, pageSize, enterpriseName));

    }

    @ApiOperation("新增企业账号")
    @PostMapping("/add")
    public boolean add (@RequestBody AddEnterpriseAccountVO enterpriseAccountVO){
        //bean拷贝
        EnterpriseAccount enterpriseAccount = new EnterpriseAccount();
        BeanUtils.copyProperties(enterpriseAccountVO, enterpriseAccount);
        //设置时间
        LocalDateTime now = LocalDateTime.now();
        enterpriseAccount.setApplicationTime(now);
        //过期时间
        LocalDateTime expireTime = null;
        if (enterpriseAccountVO.getStatus() == 0){
            //试用
            expireTime = now.plusDays(7);
        }
        if (enterpriseAccountVO.getStatus() == 1){
            //正式
            expireTime = now.plusDays(enterpriseAccountVO.getValidityDay());
        }
        if (expireTime == null){
            throw new RuntimeException("账号类型信息设置有误");
        }
        enterpriseAccount.setExpireTime(expireTime);
        return enterpriseAccountService.add(enterpriseAccount);

    }

    //根据id查询账号信息
    @ApiOperation("账号查询操作")
    @GetMapping("/getById/{id}")
    public EnterpriseAccount getById(@PathVariable("id") String id){
        return enterpriseAccountService.getById(id);
    }

    //编辑账号接口
    @ApiOperation("账号编辑操作")
    @PutMapping("/update")
    public Result update(@RequestBody UpdateEnterpriseAccountVO updateEnterpriseAccountVO){
        Result result = new Result();
        //获取原有的account
        EnterpriseAccount enterpriseAccount =
                enterpriseAccountService.getById(updateEnterpriseAccountVO.getEnterpriseId());
        if (enterpriseAccount == null){
            //说明账号不存在
            result.setStatus(ResultCode.error);
            result.setDesc("账号不存在");
            return result;
        }
        if (updateEnterpriseAccountVO.getStatus() != null){
            if (updateEnterpriseAccountVO.getStatus() == 0 && enterpriseAccount.getStatus() == 1){
                //正式使用不能改成试用期
                result.setStatus(ResultCode.error);
                result.setDesc("正式使用不能改成试用期");
                return result;
            }
            if (updateEnterpriseAccountVO.getStatus() == 1 && enterpriseAccount.getStatus() == 0){
                //试用期改为正式期
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expireTime = now.plusDays(updateEnterpriseAccountVO.getValidityDay());
                enterpriseAccount.setExpireTime(expireTime);
                enterpriseAccount.setApplicationTime(now);
            }
            if (updateEnterpriseAccountVO.getStatus() == 1 && enterpriseAccount.getStatus() == 1) {
                //正式期申请延期
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expireTime = now.plusDays(updateEnterpriseAccountVO.getValidityDay());
                enterpriseAccount.setExpireTime(expireTime);
            }
        }
        //其他字段直接进行bean拷贝
        BeanUtils.copyProperties(updateEnterpriseAccountVO, enterpriseAccount);
        boolean flag = enterpriseAccountService.updateById(enterpriseAccount);
        if (flag == true){
            result.setStatus(ResultCode.success);
            result.setDesc("修改成功");
            return result;
        }else {
            result.setStatus(ResultCode.error);
            result.setDesc("修改失败");
            return result;
        }
    }

    //账号删除
    @ApiOperation("账号删除操作")
    @DeleteMapping ("/deleteById/{id}")
    public boolean deleteById(@PathVariable("id") String id){
        boolean flag = enterpriseAccountService.removeById(id);
        return flag;
    }

    //账号还原
    @ApiOperation("账号还原操作")
    @PutMapping ("/recovery/{id}")
    public boolean recovery(@PathVariable("id") String id){
        return enterpriseAccountService.recovery(id);
    }

    //账号禁用
    @ApiOperation("账号禁用")
    @PutMapping("/forbidden/{id}")
    public boolean forbidden(@PathVariable("id") String id){
        EnterpriseAccount enterpriseAccount = enterpriseAccountService.getById(id);
        if (enterpriseAccount == null){
            return false;
        }
        enterpriseAccount.setStatus(AccountStatus.Forbidden.getStatus());
        boolean flage = enterpriseAccountService.updateById(enterpriseAccount);
        return flage;
    }
}
