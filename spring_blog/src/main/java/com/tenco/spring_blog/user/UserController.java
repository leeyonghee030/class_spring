package com.tenco.spring_blog.user;

import jakarta.persistence.Id;
import jakarta.servlet.http.HttpSession;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Slf4j
@Controller // IoC 제어에 역전 싱글톤
@RequiredArgsConstructor
public class UserController {

    private final UserPersistRepository userPersistRepository;


    //    GET http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        // templates/ 콘텐츠 루트 경로
        return "user/join-form";
    }

    @PostMapping("/join")
    public String join(UserRequest.JoinDto dto, Model model) {
        log.info("===회원가입 요청===");
        log.info("사용자명 : " + dto.getUsername());
        log.info("패스워드 : " + dto.getPassword());
        log.info("이메일 : " + dto.getEmail());

        try{
            //1. 인증 검사 유효성 검사
            dto.validate();



            // 사용자명 중복 체크
            if (userPersistRepository.findByUsername(dto.getUsername()) != null){
                throw new IllegalArgumentException("이미 존재하는 사용자 명입니다");
            }

            // DTO fnf  Entity로 변환
            userPersistRepository.save(dto.toEntity());



            //회원가입 성공시 로그인 화면으로 이동
            return  "redirect:/login";

        } catch (Exception e){
            log.error("회원가입 실패 : {} ", e.getMessage());
            model.addAttribute("errorMessage",e.getMessage());
            return "user/join-form";

        }



    }


    //    GET http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {
        // templates/ 콘텐츠 루트 경로
        return "user/login-form";
    }


    @PostMapping("/login")
    public String login(UserRequest.LoginDto dto, HttpSession session,Model model) {
        log.info("====로그인 요청===");
        log.info("사명자 명 : {}",dto.getUsername());

        try {
            //1. 입렵데이터 검증
            dto.validate();

            //2. 사용자명과 비밀번호로 사용자 조회
            User sessionUser = userPersistRepository.findByUsernameAndPassword(dto.getUsername(),dto.getPassword());
            //3. 로그인 성공/실패 처리
            if (sessionUser == null) {
                //로그인 실패 일치하는 사용자없음
                throw new IllegalArgumentException("사용자명 또는 비밀번호가 올바르지 않습니다");
            }

            //머스태치가 세샨 값을 기본으로 읽지않는 설정이 되어있음
            //머스태치 파일에서 세션 메모리에 접근할수있도록 설정 추가해야됨

            sessionUser.setPassword(null);
            //4. 로그인성공 : 세션에 사용자 정보를 저장
            session.setAttribute("sessionUser", sessionUser);

            log.info("로그인한 사용자 : {}", sessionUser.getUsername());

            //5. 메인 페이지로 리다이렉트
            return "redirect:/";


        } catch (Exception e) {
            //로그인 실패시 에러 메세지와 함께 로그인 폼으로 돌려보내기
            model.addAttribute("errorMessage", e.getMessage());
            return "user/login-form";
        }


    }

    //    GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(Model model,HttpSession session) {
        //1. 인증검사
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        User user = userPersistRepository.findById(sessionUser.getId());
        model.addAttribute("user",user);

        return "user/update-form";
    }

    @PostMapping("/user/update")
    public String update(UserRequest.UpdateDto updateDto, Model model, HttpSession session) {

        //1. 인증검사
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null){
            return "redirect:/login";
        }
        try {
            // 유효성검사
            updateDto.validate();

            //세션 동기화
            User updatedUser = userPersistRepository.updateById(sessionUser.getId(), updateDto);
            updatedUser.setPassword(null);
            session.setAttribute("sessionUser", updatedUser);

            //성공후 메인페이지로 리다이렉트
            return "redirect:/";
        } catch (Exception e) {
            log.error("회원정보 수정 실패 : {}",e.getMessage());
            model.addAttribute("user",userPersistRepository.findById(sessionUser.getId()));
            model.addAttribute("errorMessage",e.getMessage());
           return "user/update-form";
        }

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
