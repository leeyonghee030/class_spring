package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.UnauthorizedException;
import com.tenco.spring_blog._core.util.Define;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.print.attribute.standard.Media;
import java.util.List;

@Slf4j
@Controller // IoC 제어에 역전 싱글톤
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //application.yml 의 변수를 가지고 올떄 사용
    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    //테스트용
    //서버사 시작될떄 딱한번 실행되어 콘솔에 값이 찍힌다
    @PostConstruct
    public void init() {
        log.info("카카오 API키 확인 :{}", kakaoClientId);
        log.info("카카오 secret API키 확인 :{}", kakaoClientSecret);
        log.info("카카오 redirect uri 확인 :{}", kakaoRedirectUri);
    }

    @GetMapping("/kakao-login")
    public String kakaoLogin() {
        String url = "https://kauth.kakao.com/oauth/authorize"
                +"?client_id=" +kakaoClientId
                +"&redirect_uri=" +kakaoRedirectUri
                +"&response_type=code";
        return "redirect:"+url;
    }

    // 흐름 1. 인가 코드 받기 ->  액세스 토큰 요청 -> 사용자 정보 요청 -> 최초면 자동 회원가입 아니면 로그인
    @GetMapping("/oauth/kakao")
    public String kakaoCallback(@RequestParam(name = "code",required = false) String code,
                                HttpSession session){
        if (code == null) {
            throw  new UnauthorizedException("카카오 로그인이 취소 되었습니다");
        }

        ResponseEntity<UserResponse.OauthToken> tokenResponse;
        ResponseEntity<UserResponse.KakaoUserInfo> profileResponse;
        try {
            //1. 받은 인가 코드를 활용해서 토큰요청 (post)
            //http 보내는 도구 만들기
            RestTemplate restTemplate = new RestTemplate();

            //헤더만들기
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


            //바디 만들기
            MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();

            tokenParams.add("grant_type","authorization_code");
            tokenParams.add("client_id",kakaoClientId);
            tokenParams.add("redirect_uri",kakaoRedirectUri);
            tokenParams.add("code",code);
            tokenParams.add("client_secret",kakaoClientSecret);


            //바디 + 헤더
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(tokenParams, tokenHeaders);

            //요청 보내기
            // 주소, 메서드 , http메서지, 응답 타입
             tokenResponse = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    tokenRequest,
                    UserResponse.OauthToken.class
            );

             // --------액세스 토큰으로 사용자 정보 요청(카카오서버)
            HttpHeaders profileHeaders = new HttpHeaders();
            profileHeaders.setBearerAuth(tokenResponse.getBody().getAccessToken());
            profileHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity profileRequest = new HttpEntity(profileHeaders);

             profileResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    profileRequest,
                    UserResponse.KakaoUserInfo.class
            );
             UserResponse.KakaoUserInfo kakaoUserInfo = profileResponse.getBody();

             //내부로직 -> 최초 소셜 로그인 사용자라면 자동 회원가입 (필요한 정보가 더있다면 추가기입)
            // 이미 회원가임 되어있는 사용자라면  -> 로그인

            SessionUserDto sessionUserDto = userService.kakaoLogin(kakaoUserInfo);
            session.setAttribute(Define.SESSION_USER, sessionUserDto);

            return "redirect:/";
        } catch (HttpClientErrorException e) {
            //카카오가 4xx 를 응답한 경우
            log.error("카카오 API 오류 : {}", e.getResponseBodyAsString());
            throw new UnauthorizedException("카카오 로그인에 실패했습니다");
        }
    }



    @PostMapping("/user/profile-image/delete")
    public String deleteProfileImage(HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        userService.deleteProfileImage(sessionUserDto.getId());
        return "redirect:/user/detail";
    }



    //http://localhost:8080/user/detail
    @GetMapping("/user/detail")
    public String detail(Model model, HttpSession session){
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        //조회후
        UserResponse.DetailDto user = userService.findDetail(sessionUserDto.getId());
        model.addAttribute("user",user);
        return "user/detail";
    }


    //    GET http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        // templates/ 콘텐츠 루트 경로
        return "user/join-form";
    }

    @PostMapping("/join")
    public String join(UserRequest.JoinDto dto, Model model) {

        dto.validate();
        userService.join(dto);

        return "redirect:/login";

    }


    //    GET http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {
        // templates/ 콘텐츠 루트 경로
        return "user/login-form";
    }


    @PostMapping("/login")
    public String login(UserRequest.LoginDto dto, HttpSession session, Model model) {

        dto.validate();

        SessionUserDto sessionUserDto = userService.login(dto);

        session.setAttribute(Define.SESSION_USER, sessionUserDto);

        return "redirect:/";

    }

    //    GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(Model model, HttpSession session) {
        //1. 인증검사
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);


        UserResponse.updateFormDto updateFormDto = userService.findUpdateForm(sessionUserDto.getId());
        model.addAttribute("user", updateFormDto);



        return "user/update-form";
    }

    @PostMapping("/user/update")
    public String update(UserRequest.UpdateDto updateDto, Model model, HttpSession session) {

        //1. 인증검사
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        // 유효성검사
        updateDto.validate();

        //세션 동기화
        SessionUserDto updateSessionUserDto = userService.updateById(sessionUserDto.getId(), updateDto);
        session.setAttribute(Define.SESSION_USER, updateSessionUserDto);

        //성공후 메인페이지로 리다이렉트
        return "redirect:/user/detail";

    }

    //    GET http://localhost:8080/logout
    @GetMapping("/logout")
    public String logoutForm(HttpSession session) {
        log.info("===로그아웃 요청===");

        session.invalidate();
        log.info("로그아웃 완료");
        // templates/ 콘텐츠 루트 경로
        return "redirect:/";
    }


}
