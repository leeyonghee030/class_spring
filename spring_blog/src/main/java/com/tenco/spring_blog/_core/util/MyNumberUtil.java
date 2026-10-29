package com.tenco.spring_blog._core.util;

// 숫자 포맷 관련 유틸리티 클래스
public class MyNumberUtil {

    // 1000 단위 콤마 포맷으로 변환
    public static String numberFormat(int number) {
        return String.format("%,d", number);
    }
}
