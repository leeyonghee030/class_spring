package com.tenco.spring_blog.sms;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.error.UnauthorizedException;
import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SmsApiController {

    private final SmsService smsService;

    @PostMapping("/api/sms/send")
    public ResponseEntity<?> smsSendCode(@RequestBody SmsRequest.phoneCheckDto phoneCheckDto, HttpSession session) {
        phoneCheckDto.validate();
        if (!smsService.checkSendTime(phoneCheckDto.getPhone(), session)){
            return ResponseEntity.badRequest().body(Map.of("message", Define.SMS_EXPIRE_MINUTES+ "분 후 다시 발송 가능합니다"));
        }
        smsService.sendVerificationCode(phoneCheckDto.getPhone(),session);

        return ResponseEntity.ok(Map.of("message", "인증번호가 발송되었습니다 "
                + Define.CODE_EXPIRE_MINUTES+ "분 안에 입력하세요"));
    }

    @PostMapping("/api/sms/verify")
    public ResponseEntity<?> smsVerifyCode(@RequestBody SmsRequest.phoneCheckDto phoneCheckDto , HttpSession session) {
        phoneCheckDto.validateCode();
        boolean verified = smsService.verifyCode(phoneCheckDto.getPhone(), phoneCheckDto.getCode(),session);
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

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> exceptionUnauthorized(UnauthorizedException e) {
        log.warn("API 401 - {}", e.getMessage());
        return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(Exception500.class)
    public ResponseEntity<?> exception500(Exception500 e) {
        log.error("API 500 - {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(Map.of("message", "서버 오류가 발생했습니다"));
    }
}
