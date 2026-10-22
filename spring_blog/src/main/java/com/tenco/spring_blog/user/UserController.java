package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception403;
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
    private  final  KakaoLoginService kakaoLoginService;






    @GetMapping("/kakao-login")
    public String kakaoLogin() {
       return "redirect:" + kakaoLoginService.getAuthorizeUrl();
    }

    // 흐름 1. 인가 코드 받기 ->  액세스 토큰 요청 -> 사용자 정보 요청 -> 최초면 자동 회원가입 아니면 로그인
    @GetMapping("/oauth/kakao")
    public String kakaoCallback(@RequestParam(name = "code",required = false) String code,
                                HttpSession session){
        SessionUserDto sessionUserDto = kakaoLoginService.login(code);
        session.setAttribute(Define.SESSION_USER, sessionUserDto);
            return "redirect:/";
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
    public String join(UserRequest.JoinDto dto, Model model, HttpSession session) {

        dto.validate();

        String email = (String) session.getAttribute(Define.VERIFIED_EMAIL);

        userService.join(dto,email); // 검증은 서비스 내부에서 처리
        session.removeAttribute(Define.VERIFIED_EMAIL);

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
