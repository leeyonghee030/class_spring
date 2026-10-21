package com.tenco.spring_blog.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

//카카오 서버와 HTTP 통신만 담당하는 클래스
//
@Slf4j
@Component
public class KakaoApiClient {

    //application.yml 의 변수를 가지고 올떄 사용
    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    //인가 요청 주소
    private static String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
    //액서스 토큰 요청 주소
    private static String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    //프로필 이미지 요청주소
    private static String PROFILE_URL = "https://kapi.kakao.com/v2/user/me";

    //한번 만들면 여러 요청에서 함께 써도 안전하게 설계되어 하나만 만들어 재사용함
    private final RestTemplate restTemplate = new RestTemplate();

    //1.  카카오 로그인 화면 주소만들기  (로그인 + 동의 할목 화면)

    public String getAuthorizeUrl() {
        return AUTHORIZE_URL
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri
                + "&response_type=code";
    }


    //2. 인가 코드로 액세스 토큰 요청 (Post , 폼데이터)
    public UserResponse.OauthToken requestAccessToken(String code) {

        //헤더만들기
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        //바디 만들기
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);
        params.add("client_secret", kakaoClientSecret);


        //바디 + 헤더
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(params, headers);

        //요청 보내기
        // 주소, 메서드 , http메서지, 응답 타입
        ResponseEntity<UserResponse.OauthToken> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                tokenRequest,
                UserResponse.OauthToken.class
        );
        return response.getBody();
    }

    //3. 액세스 토큰 으로 사용자 정보 요청 (GET)
    public UserResponse.KakaoUserInfo requestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity request = new HttpEntity(headers);

        ResponseEntity<UserResponse.KakaoUserInfo> response = restTemplate.exchange(
                PROFILE_URL,
                HttpMethod.GET,
                request,
                UserResponse.KakaoUserInfo.class
        );

        return  response.getBody();
    }

}
