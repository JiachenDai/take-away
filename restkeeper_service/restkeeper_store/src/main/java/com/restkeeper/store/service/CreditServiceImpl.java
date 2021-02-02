package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditCompanyUser;
import com.restkeeper.store.mapper.CreditMapper;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service("creditService")
@Service(version = "1.0.0",protocol = "dubbo")
public class CreditServiceImpl extends ServiceImpl<CreditMapper, Credit> implements ICreditService {

    @Autowired
    @Qualifier("creditCompanyUserService")
    private ICreditCompanyUserService creditCompanyUserService;


    @Override
    @Transactional
    public boolean add(Credit credit, List<CreditCompanyUser> users) {
        //保存挂账的基础信息
        this.save(credit);
        if (users != null && !users.isEmpty()){
            //进行用户名去重的校验
            //获取用户名列表
            List<String> userNameList = users.stream().map(d -> d.getUserName()).collect(Collectors.toList());
            long count = userNameList.stream().distinct().count();
            if (count != users.size()){
                throw new RuntimeException("用户名重复");
            }

            //设置关联
            for (CreditCompanyUser user : users) {
                user.setCreditId(credit.getCreditId());
            }
            creditCompanyUserService.saveBatch(users);
        }
        return true;
    }

    @Override
    public IPage<Credit> queryPage(int pageNum, int pageSize, String name) {
        IPage<Credit> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Credit> creditQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)){
            creditQueryWrapper.lambda().like(Credit::getUserName, name)
                    .or().inSql(Credit::getCreditId, "select credit_id from t_credit_company_user where user_name like '%"
                    + StringEscapeUtils.escapeSql(name) + "%'");
        }
        page = this.page(page, creditQueryWrapper);
        //如果查询出来的记录挂账类型是公司的话，还需要设置相关联的用户的信息
        List<Credit> records = page.getRecords();
        for (Credit record : records) {
            if (record.getCreditType() == SystemCode.CREDIT_TYPE_COMPANY){
                QueryWrapper<CreditCompanyUser> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(CreditCompanyUser::getCreditId, record.getCreditId());
                record.setUsers(creditCompanyUserService.list(queryWrapper));
            }
        }
        return page;
    }

    @Override
    public Credit queryById(String id) {
        if (StringUtils.isEmpty(id)){
            throw new RuntimeException("id为空");
        }
        Credit credit = this.getById(id);
        if (credit == null){
            throw new RuntimeException("没有此挂账记录");
        }
        //如果挂账是企业挂账，则添加关联用户信息
        if (credit.getCreditType() == SystemCode.CREDIT_TYPE_COMPANY){
            QueryWrapper<CreditCompanyUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(CreditCompanyUser::getCreditId, credit.getCreditId());
            List<CreditCompanyUser> users = creditCompanyUserService.list(queryWrapper);
            credit.setUsers(users);
        }
        return credit;
    }

    @Override
    @Transactional
    public boolean updateInfo(Credit credit, List<CreditCompanyUser> users) {
        if (credit == null){
            return false;
        }
        //修改基础信息
        this.updateById(credit);
        if (users != null && users.size() != 0 && credit.getCreditType() == SystemCode.CREDIT_TYPE_COMPANY){
            QueryWrapper<CreditCompanyUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(CreditCompanyUser::getCreditId, credit.getCreditId());
            creditCompanyUserService.remove(queryWrapper);
            //进行用户名去重的校验
            //获取用户名列表
            List<String> userNameList = users.stream().map(d -> d.getUserName()).collect(Collectors.toList());
            long count = userNameList.stream().distinct().count();
            if (count != users.size()){
                throw new RuntimeException("用户名重复");
            }

            //设置关联
            for (CreditCompanyUser user : users) {
                user.setCreditId(credit.getCreditId());
            }
            creditCompanyUserService.saveBatch(users);
        }
        return true;
    }
}
