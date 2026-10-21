package com.tenco.spring_blog.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@Slf4j
@Component
public class GoogleApiClient {

    //application.yml 의 변수를 가지고 올떄 사용
    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    //인가 요청 주소
    private static String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    //액서스 토큰 요청 주소
    private static String TOKEN_URL = "https://oauth2.googleapis.com/token";
    //프로필 이미지 요청주소 (OpenID Connect 형식: sub, email_verified 필드를 반환)
    private static String PROFILE_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    //한번 만들면 여러 요청에서 함께 써도 안전하게 설계되어 하나만 만들어 재사용함
    private final RestTemplate restTemplate = new RestTemplate();

    public String getAuthorizeUrl(String state) {
        return AUTHORIZE_URL
                + "?client_id=" + googleClientId
                + "&redirect_uri=" + googleRedirectUri
                + "&response_type=code"
                + "&scope=openid%20email%20profile"
                + "&state=" + state;

    }

    //2. 인가 코드로 액세스 토큰 요청 (Post , 폼데이터)
    public UserResponse.OauthToken requestAccessToken(String code) {

        //헤더만들기
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        //바디 만들기
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();


        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", googleRedirectUri);



        //바디 + 헤더
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(params, headers);

        //요청 보내기
        // 주소, 메서드 , http메서지, 응답 타입
        ResponseEntity<UserResponse.OauthToken> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                tokenRequest,
                UserResponse.OauthToken.class
        );
        return response.getBody();
    }

    //3. 액세스 토큰 으로 사용자 정보 요청 (GET)
    public UserResponse.GoogleUserInfo requestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity request = new HttpEntity(headers);

        ResponseEntity<UserResponse.GoogleUserInfo> response = restTemplate.exchange(
                PROFILE_URL,
                HttpMethod.GET,
                request,
                UserResponse.GoogleUserInfo.class
        );

        return  response.getBody();
    }

    public boolean hasRequiredScopes(UserResponse.OauthToken token) {
        if (token == null || token.getScope() == null) {
            return false;
        }
        Set<String> grantedScopes =
                new HashSet<>(Arrays.asList(
                        token.getScope().split("\\s+")
                ));

        return grantedScopes.contains("openid")
                && grantedScopes.contains("email")
                && grantedScopes.contains("profile");

    }






}
