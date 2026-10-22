package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;
    private final MailService mailService;

    //Post /api/email/send
    //요청바디 : {"email": "a@naver.com"}
    @PostMapping("/api/email/send")
    public ResponseEntity<?> sendCode(@RequestBody UserRequest.EmailCheckDto emailCheckDto, HttpSession session) {
        //인증검사 뺴야함
        emailCheckDto.validate();
        // 이미 가입된 이메일이 있으면 400 던지기
        userService.checkEmailAvailable(emailCheckDto.getEmail());

        // SMTP를 사용해서 인증 번호 발송 (구글 메일 서버 -> 사용자 이메일 수신함 (인증번호))
        mailService.sendVerificationCode(emailCheckDto.getEmail(),session);

        return ResponseEntity.ok(Map.of("message", "인증 되었습니다"));
    }

    // 요청바디 {"email" : "a@naver.com", "code" : "123456"}
    @PostMapping("/api/email/verify")
    public ResponseEntity<?> verifyCode(@RequestBody UserRequest.EmailCheckDto emailCheckDto , HttpSession session) {
        emailCheckDto.validateCode();
        boolean verified = mailService.verifyCode(emailCheckDto.getEmail(), emailCheckDto.getCode(),session);
        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다"));
        }
        return ResponseEntity.ok().body(Map.of("message", "인증되었습니다"));
    }

    //이 컨트롤러에서 난 예외만 처리 (화면용 컨트롤러는 GlobalExceptionHandler가 계속 처리)
    //GlobalExceptionHandler의 ex400은 @ResponseBody가 없어 뷰(err/400 HTML)를 리턴하므로
    //fetch의 response.json()이 파싱에 실패해 "서버와 통신하지 못했습니다"로 잘못 표시되는 문제를 막기 위함
    @ExceptionHandler(Exception400.class)
    public ResponseEntity<?> exception400(Exception400 e) {
        log.warn("API 400 - {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(Exception500.class)
    public ResponseEntity<?> exception500(Exception500 e) {
        log.error("API 500 - {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(Map.of("message", "서버 오류가 발생했습니다"));
    }
}

