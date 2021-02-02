package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditRepayment;
import com.restkeeper.store.mapper.CreditRepaymentMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service("creditRepaymentService")
@Service(version = "1.0.0",protocol = "dubbo")
public class CreditRepaymentServiceImpl extends ServiceImpl<CreditRepaymentMapper, CreditRepayment> implements ICreditRepaymentService {

    @Autowired
    @Qualifier("creditService")
    private ICreditService creditService;

    @Override
    @Transactional
    public boolean repayment(CreditRepayment creditRepayment) {
        //获取当前用户的还款金额
        Integer repaymentAmount = creditRepayment.getRepaymentAmount();
        //比对，还款金额不能超过欠款金额
        Credit credit = creditService.getById(creditRepayment.getCreditId());
        if (credit == null){
            throw new RuntimeException("不存在此挂账信息");
        }
        if (credit.getCreditAmount() - repaymentAmount < 0){
            throw new RuntimeException("还款金额大于欠款金额");
        }
        //新增还款金额
        this.save(creditRepayment);
        //更新欠款额度
        credit.setCreditAmount(credit.getCreditAmount() - repaymentAmount);
        //设置总还款金额
        credit.setTotalRepaymentAmount(credit.getTotalRepaymentAmount() + repaymentAmount);
        return creditService.saveOrUpdate(credit);
    }
}
