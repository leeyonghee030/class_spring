package com.tenco.spring_blog.sms;

import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.util.Define;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SolapiClient {

    private final String solapiPhone;
    private final DefaultMessageService messageService;

    public SolapiClient(@Value("${solapi.api-key}") String solapiClientId,
                      @Value("${solapi.api-secret}") String solapiClientSecret,
                      @Value("${solapi.sender}") String solapiPhone) {
        this.solapiPhone = solapiPhone;
        this.messageService = com.solapi.sdk.SolapiClient.INSTANCE.createInstance(solapiClientId, solapiClientSecret);
    }



    //1. 인증번호 발송
    public void sendVerificationCode(String phone,String code) {
        Message message = new Message();
        message.setFrom(solapiPhone);
        message.setTo(phone);
        message.setText("[스프링블로그]\n인증번호 " + code+"\n" + "(" + Define.CODE_EXPIRE_MINUTES + "분 안에 입력)");
        try {
            messageService.send(message);
        } catch (SolapiMessageNotReceivedException exception) {
            log.error("SMS 발송 실패 - 수신번호: {}, 실패목록: {}", phone, exception.getFailedMessageList());
            throw new Exception500("문자 작성 중 오류가 발생했습니다");
        } catch (Exception exception) {
            log.error("SMS 발송 실패 - 수신번호: {}, {}", phone, exception.getMessage());
            throw new Exception500("문자 작성 중 오류가 발생했습니다");
        }
    }




}
