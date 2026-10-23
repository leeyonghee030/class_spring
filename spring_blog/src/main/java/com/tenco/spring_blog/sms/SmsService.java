package com.tenco.spring_blog.sms;


import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog._core.util.MailUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsService {


    private final SolapiClient solapiClient;




    private record SmsCode(String code, LocalDateTime sentAt)implements Serializable {
        boolean isExpired(){
            return LocalDateTime.now().isAfter(sentAt.plusMinutes(Define.CODE_EXPIRE_MINUTES));
        }
        boolean isSmsExpired(){
            return LocalDateTime.now().isAfter(sentAt.plusMinutes(Define.SMS_EXPIRE_MINUTES));
        }
    }

    //1. 인증번호 발송
    public  boolean sendVerificationCode(String phone, HttpSession session) {
        //1-1 6자리 인증번호 생성
        String code = MailUtil.generateCode();
        if (!checkSendTime(phone, session)){
            return false;
        }
        solapiClient.sendVerificationCode(phone,code);
        session.setAttribute(Define.SMS_CODE_PREFIX + phone,
                new SmsCode(code,LocalDateTime.now()));
        return true;
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
