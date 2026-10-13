package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller // IoC 제어에 역전 싱글톤
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


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
        return "redirect:/";

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
