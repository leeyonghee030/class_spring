package com.tenco.spring_blog.sms;


import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog._core.util.MailUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;

@Service
@Slf4j
public class SmsService {

    private final String solapiPhone;
    private final DefaultMessageService messageService;

    public SmsService(@Value("${solapi.api-key}") String solapiClientId,
                       @Value("${solapi.api-secret}") String solapiClientSecret,
                       @Value("${solapi.sender}") String solapiPhone) {
        this.solapiPhone = solapiPhone;
        this.messageService = SolapiClient.INSTANCE.createInstance(solapiClientId, solapiClientSecret);
    }



    private record SmsCode(String code, LocalDateTime sentAt)implements Serializable {
        boolean isExpired(){
            return LocalDateTime.now().isAfter(sentAt.plusMinutes(Define.CODE_EXPIRE_MINUTES));
        }
        boolean isSmsExpired(){
            return LocalDateTime.now().isAfter(sentAt.plusMinutes(Define.SMS_EXPIRE_MINUTES));
        }
    }

    //1. 인증번호 발송
    public void sendVerificationCode(String phone, HttpSession session) {
        //1-1 6자리 인증번호 생성
        String code = MailUtil.generateCode();
        Message message = new Message();
        message.setFrom(solapiPhone);
        message.setTo(phone);
        message.setText("[스프링블로그 이용희]\n인증번호 " + code+"\n" + "(" + Define.CODE_EXPIRE_MINUTES + "분 안에 입력)");
        try {
            messageService.send(message);
        } catch (SolapiMessageNotReceivedException exception) {
            log.error("SMS 발송 실패 - 수신번호: {}, 실패목록: {}", phone, exception.getFailedMessageList());
            throw new Exception500("문자 작성 중 오류가 발생했습니다");
        } catch (Exception exception) {
            log.error("SMS 발송 실패 - 수신번호: {}, {}", phone, exception.getMessage());
            throw new Exception500("문자 작성 중 오류가 발생했습니다");
        }
        session.setAttribute(Define.SMS_CODE_PREFIX + phone,
                new SmsCode(code,LocalDateTime.now()));
    }

    public boolean verifyCode(String phone, String code, HttpSession session) {
        String key = Define.SMS_CODE_PREFIX + phone;
        SmsCode saveSmsCode = (SmsCode) session.getAttribute(key);
        if (saveSmsCode == null) {
            throw new Exception400("인증번호를 먼저 요청해주세요");
        }
        if (saveSmsCode.isExpired()){
            session.removeAttribute(key); // 만료된 인증번호는 세션에서 삭제
            throw new Exception400("인증번호가 만료되었습니다. 다시 요청해주세요");
        }
        if (!saveSmsCode.code.equals(code)){
            throw new Exception400("인증번호가 일치하지 않습니다");
        }
        session.removeAttribute(key);
        session.setAttribute(Define.VERIFIED_PHONE,phone);
        return true;
    }

    public boolean checkSendTime(String phone,HttpSession session) {
        String key = Define.SMS_CODE_PREFIX + phone;
        SmsCode saveSmsCode = (SmsCode) session.getAttribute(key);
        if (saveSmsCode == null || saveSmsCode.isSmsExpired()){
            return true; // 보낸 기록이 없거나 1분이 지났으면 재발송 허용
        }
        return false; // 1분이 지나지 않았으면 재발송 차단
    }
}
