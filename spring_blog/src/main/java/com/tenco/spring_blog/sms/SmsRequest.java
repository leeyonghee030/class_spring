package com.tenco.spring_blog.sms;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;

public class SmsRequest {
    @Data
    public static class phoneCheckDto{
        private String phone;
        private String code;

        public phoneCheckDto(String phone, String code) {
            this.phone = (phone == null) ? null:phone.replaceAll("\\D", "");
            this.code = code;
        }

        public void validate() {
            if (phone == null || phone.trim().isEmpty()) {
                throw new Exception400("휴대폰 번호를 입력해주세요");
            }
            if (!phone.matches("^01[0-9]\\d{7,8}$")){
                throw new Exception400("휴대폰 번호를 입력해주세요(ex01012341234)");
            }
        }
        //인증번호 확인 API에서 추가로 호출할 예정
        public void validateCode() {
            validate();
            if (code== null || code.trim().isEmpty()) {
                throw new Exception400("인증번호를 입력해주세요");
            }
        }

    }
}
