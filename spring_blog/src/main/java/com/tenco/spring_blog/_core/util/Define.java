package com.tenco.spring_blog._core.util;

public class Define {

    public static final String SESSION_USER = "sessionUserDto";

    // OAuth CSRF 방지용 state 값을 담아두는 세션 키 (로그인 시작 ~ 콜백까지 임시 보관)
    public static final String KAKAO_OAUTH_STATE = "kakaoOauthState";
    public static final String GOOGLE_OAUTH_STATE = "googleOauthState";
}
