package com.tenco.spring_blog._core.util;

import java.security.SecureRandom;
import java.util.Random;

public class MailUtil {
   //시큐라타 - 예측하기 어려운 난수 생성기 (보안용도)
    private static final SecureRandom RANDOM = new SecureRandom();

    // 6자리 인증 번호 생성 10000 - 99999
    public static String generateCode() {
        int code = 100000 + RANDOM.nextInt(900000); // 0-899999까지
        return String.valueOf(code);
    }

}
