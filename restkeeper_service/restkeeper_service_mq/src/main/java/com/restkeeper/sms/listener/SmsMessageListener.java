package com.restkeeper.sms.listener;

import com.alibaba.alicloud.sms.ISmsService;
import com.alibaba.fastjson.JSON;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.sms.SmsObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsMessageListener {

    @Autowired
    private ISmsService smsService;

    @RabbitListener(queues = SystemCode.SMS_ACCOUNT_QUEUE)
    public void getAccountMessage(String message){
        log.info("发送短信的监听类接收到了消息：" + message);
        //转换参数
        SmsObject smsObject = JSON.parseObject(message, SmsObject.class);
        //基于sms组件进行短信发送
        SendSmsResponse smsResponse = this.sendSms(smsObject.getPhoneNumber(), smsObject.getSignName(),
                smsObject.getTemplateCode(), smsObject.getTemplateJsonParam());
        log.info(JSON.toJSONString(smsResponse));
    }

    //发送手机短信
    private SendSmsResponse sendSms(String phoneNumber, String signName, String templateCode, String templateJsonParam) {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phoneNumber);
        request.setSignName(signName);
        request.setTemplateCode(templateCode);
        request.setTemplateParam(templateJsonParam);
        SendSmsResponse sendSmsResponse;
        try {
            sendSmsResponse  = smsService.sendSmsRequest(request);
        } catch (ClientException e) {
            e.printStackTrace();
            sendSmsResponse = new SendSmsResponse();
        }
        return sendSmsResponse;
    }
}
