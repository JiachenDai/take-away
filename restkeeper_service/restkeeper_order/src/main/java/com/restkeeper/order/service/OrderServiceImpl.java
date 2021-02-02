package com.restkeeper.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.aop.TenantAnnotation;
import com.restkeeper.constants.OrderDetailType;
import com.restkeeper.constants.OrderPayType;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.dto.CreditDTO;
import com.restkeeper.dto.DetailDTO;
import com.restkeeper.entity.OrderDetailEntity;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.order.mapper.OrderMapper;
import com.restkeeper.service.IOrderDetailService;
import com.restkeeper.service.IOrderService;
import com.restkeeper.store.entity.*;
import com.restkeeper.store.service.*;
import com.restkeeper.tenant.TenantContext;
import com.restkeeper.utils.SequenceUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service("orderService")
@Service(version = "1.0.0",protocol = "dubbo")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements IOrderService {

    @Autowired
    @Qualifier("orderDetailService")
    private IOrderDetailService orderDetailService;

    @Reference(version = "1.0.0", check = false)
    private ISellCalculationService sellCalculationService;

    @Reference(version = "1.0.0", check = false)
    private ITableService tableService;


    @Reference(version = "1.0.0", check = false)
    private ICreditService creditService;

    @Reference(version = "1.0.0", check = false)
    private ICreditCompanyUserService creditCompanyUserService;


    @Reference(version = "1.0.0", check = false)
    private ICreditLogService creditLogService;

    @Reference(version = "1.0.0", check = false)
    private ITableLogService tableLogService;


    @Override
    @Transactional
    public String addOrder(OrderEntity orderEntity) {
        //生成流水号
        if (StringUtils.isEmpty(orderEntity.getOrderNumber())){
            //因为后续加菜的时候也会调用这个service，因此如果已经有值了则无需生成流水号
            String storeId = RpcContext.getContext().getAttachment("storeId");
            orderEntity.setOrderNumber(SequenceUtils.getSequence(storeId));
        }
        this.saveOrUpdate(orderEntity);
        //操作订单详情
        List<OrderDetailEntity> orderDetails = orderEntity.getOrderDetails();
        for (OrderDetailEntity orderDetail : orderDetails) {
            //建立与order的关联关系
            orderDetail.setOrderId(orderEntity.getOrderId());
            //生成detail自己的流水
            orderDetail.setOrderNumber(SequenceUtils.getSequenceWithPrefix(orderEntity.getOrderNumber()));

            //完成沽清相关操作
            //然而在进行沽清服务的相关调用前，需要先往TenantContext中存入多租户相关的值
            TenantContext.addAttachment("storeId", RpcContext.getContext().getAttachment("storeId"));
            TenantContext.addAttachment("shopId", RpcContext.getContext().getAttachment("shopId"));
            Integer remainderCount = sellCalculationService.getRemainderCount(orderDetail.getDishId());
            if (remainderCount != -1){
                if (remainderCount < orderDetail.getDishNumber()){
                    throw new RuntimeException(orderDetail.getDishName() + "超过沽清设置数量");
                }
                //沽清扣减
                sellCalculationService.decrease(orderDetail.getDishId(), orderDetail.getDishNumber());
            }
        }
        orderDetailService.saveBatch(orderDetails);
        return orderEntity.getOrderId();
    }

    @Override
    @Transactional
    @TenantAnnotation
    public boolean returnDish(DetailDTO detailDTO) {
        //操作order_detail表根据detailId来获取要退的菜的信息
        String detailId = detailDTO.getDetailId();
        OrderDetailEntity orderDetailEntity = orderDetailService.getById(detailId);
        Integer status = orderDetailEntity.getDetailStatus();
        if (status == OrderDetailType.PLUS_DISH.getType() || OrderDetailType.NORMAL_DISH.getType() == status){
            if (orderDetailEntity.getDishNumber() <= 0){
                throw new RuntimeException(orderDetailEntity.getDishName() + "已经被退完");
            }

            //产生新的订单详情记录
            OrderDetailEntity returnOrderDetailEntity = new OrderDetailEntity();
            BeanUtils.copyProperties(orderDetailEntity, returnOrderDetailEntity);
            returnOrderDetailEntity.setDetailId(null);
            returnOrderDetailEntity.setStoreId(null);
            returnOrderDetailEntity.setShopId(null);
            //产生流水号
            returnOrderDetailEntity.setOrderNumber(SequenceUtils.getSequenceWithPrefix(orderDetailEntity.getOrderNumber()));
            returnOrderDetailEntity.setDetailStatus(OrderDetailType.RETURN_DISH.getType());
            returnOrderDetailEntity.setDishNumber(1);
            returnOrderDetailEntity.setReturnRemark(detailDTO.getRemarks().toString());
            orderDetailService.save(returnOrderDetailEntity);
            //修改原有订单详情信息
            orderDetailEntity.setDishNumber(orderDetailEntity.getDishNumber() - 1);
            orderDetailEntity.setDishAmount(orderDetailEntity.getDishNumber() * orderDetailEntity.getDishPrice());
            orderDetailService.updateById(orderDetailEntity);

            //修改原有订单信息
            OrderEntity order = this.getById(orderDetailEntity.getOrderId());
            order.setTotalAmount(order.getTotalAmount() - orderDetailEntity.getDishPrice());
            this.updateById(order);

            //进行沽清操作
//            TenantContext.addAttachment("storeId", RpcContext.getContext().getAttachment("storeId"));
//            TenantContext.addAttachment("shopId", RpcContext.getContext().getAttachment("shopId"));
            Integer remainderCount = sellCalculationService.getRemainderCount(orderDetailEntity.getDishId());
            if (remainderCount > 0){
                //进行沽清操作+1
                sellCalculationService.add(orderDetailEntity.getDishId(), 1);
            }
        }else {
            throw new RuntimeException("不支持退菜操作");
        }
        return true;
    }

    @Override
    @Transactional
    @TenantAnnotation
    public boolean pay(OrderEntity orderEntity) {
        //修改订单主表信息
        this.updateById(orderEntity);
        //修改桌台状态
        Table table = tableService.getById(orderEntity.getTableId());
        table.setStatus(SystemCode.TABLE_STATUS_FREE);
        tableService.updateById(table);
        return true;
    }

    @Override
    @Transactional
    @TenantAnnotation
    public boolean pay(OrderEntity orderEntity, CreditDTO creditDTO) {
        this.updateById(orderEntity);
        //设置挂账信息
        if (orderEntity.getPayType() == OrderPayType.CREDIT.getType()){
            Credit credit = creditService.getById(creditDTO.getCreditId());
            //判断挂账类型如果是个人挂账
            if (credit.getCreditType() == SystemCode.CREDIT_TYPE_USER){
                if (!credit.getUserName().equals(creditDTO.getCreditUserName())){
                    throw new RuntimeException("挂账人信息不同，不允许挂账");
                }
                credit.setCreditAmount(credit.getCreditAmount() + creditDTO.getCreditAmount());
                creditService.saveOrUpdate(credit);
            }

            //公司用户
            List<CreditCompanyUser> companyUsers = null;
            if (credit.getCreditType() == SystemCode.CREDIT_TYPE_COMPANY){
                //不能直接在这里进行service.list()的调用，因为dubbo对于lambda的序列化有问题
                List<CreditCompanyUser> companyUserList = creditCompanyUserService.getInfoList(creditDTO.getCreditId());
                //判断当前的挂账人在这个list中是否存在
                Optional<CreditCompanyUser> resultInfo = companyUserList.stream().filter(user -> user.getUserName().equals(creditDTO.getCreditUserName())).findFirst();
                if (!resultInfo.isPresent()){
                    throw new RuntimeException("当前用户不在该公司中，请联系管家端进行设置");
                }
                companyUsers = companyUserList;
            }
            //挂账明细
            CreditLogs creditLogs = new CreditLogs();
            creditLogs.setCreditId(creditDTO.getCreditId());
            creditLogs.setOrderId(orderEntity.getOrderId());
            creditLogs.setType(credit.getCreditType());
            creditLogs.setCreditAmount(creditDTO.getCreditAmount());
            creditLogs.setOrderAmount(orderEntity.getTotalAmount());
            creditLogs.setReceivedAmount(orderEntity.getTotalAmount());
            creditLogs.setCreditAmount(creditDTO.getCreditAmount());
            if (credit.getCreditType() == SystemCode.CREDIT_TYPE_COMPANY){
                creditLogs.setUserName(creditDTO.getCreditUserName());
                creditLogs.setCompanyName(credit.getCompanyName());
                Optional<CreditCompanyUser> optional = companyUsers.stream().filter(user -> user.getUserName().equals(creditDTO.getCreditUserName())).findFirst();
                String phone = optional.get().getPhone();
                creditLogs.setPhone(phone);
            }else if (credit.getCreditType() == SystemCode.CREDIT_TYPE_USER){
                creditLogs.setUserName(creditDTO.getCreditUserName());
                creditLogs.setPhone(credit.getPhone());
            }
            creditLogService.save(creditLogs);
            //修改桌台状态为空闲
            Table table = tableService.getById(orderEntity.getTableId());
            table.setStatus(SystemCode.TABLE_STATUS_FREE);
            tableService.updateById(table);
        }
        return true;
    }

    @Override
    @Transactional
    @TenantAnnotation
    public boolean changeTable(String orderId, String targetTableId) {
        String loginUserName = TenantContext.getLoginUserName();
        if (StringUtils.isEmpty(orderId)){
            throw new RuntimeException("订单id为空");
        }
        if (StringUtils.isEmpty(targetTableId)){
            throw new RuntimeException("目标桌台id为空");
        }
        OrderEntity orderEntity = this.getById(orderId);
        //释放原有桌台
        String orderEntityTableId = orderEntity.getTableId();
        Table table = tableService.getById(orderEntityTableId);
        table.setStatus(SystemCode.TABLE_STATUS_FREE);
        tableService.updateById(table);
        //更改新的桌台状态
        Table targetTable = tableService.getById(targetTableId);
        if (targetTable == null){
            throw new RuntimeException("目标桌台不存在");
        }
        if (targetTable.getStatus() != SystemCode.TABLE_STATUS_FREE){
            throw new RuntimeException("目标桌台不空闲");
        }
        targetTable.setStatus(SystemCode.TABLE_STATUS_OPEND);
        tableService.updateById(targetTable);
        //记录桌台日志信息
        TableLog tableLog =new TableLog();
        tableLog.setTableStatus(SystemCode.TABLE_STATUS_OPEND);
        tableLog.setCreateTime(LocalDateTime.now());
        tableLog.setTableId(targetTableId);
        tableLog.setUserNumbers(orderEntity.getPersonNumbers());

        tableLog.setUserId(loginUserName);
        tableLogService.save(tableLog);
        //建立order和新的桌台的关联
        orderEntity.setTableId(targetTableId);
        this.updateById(orderEntity);
        return true;
    }
}
