package com.tenco.spring_blog._core.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice //모든 컨트롤러에서 발생하는 예외를 이 클래스에서 처리 (중앙집중화) IoC
public class GlobalExceptionHandler {

    // 특정 예외 타입이 발생했을 떄 실행될 메서그로 지정
    @ExceptionHandler(Exception400.class)
    public String ex400(Exception400 e, HttpServletRequest request, Model model) {
        log.warn("=== 400 Bad Request error ===");
        log.warn("요청 URL : {}", request.getRequestURI());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        model.addAttribute("msg",e.getMessage());
        return "err/400";
    }

    @ExceptionHandler(Exception401.class)
    public String ex401(Exception401 e, HttpServletRequest request, RedirectAttributes rttr) {
        log.warn("=== 401 Unauthorized error ===");
        log.warn("요청 URL : {}", request.getRequestURI());
        log.warn("인증 오류: {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        rttr.addFlashAttribute("errorMessage",e.getMessage());
        return "redirect:/login";
    }

//    @ExceptionHandler(Exception403.class)
//    public String ex403(Exception403 e, HttpServletRequest request, Model model) {
//        log.warn("=== 403 Forbidden error ===");
//        log.warn("요청 URL : {}", request.getRequestURI());
//        log.warn("권한 오류: {}", e.getMessage());
//        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
//
//        model.addAttribute("msg",e.getMessage());
//        return "err/403";
//    }
    @ResponseStatus(HttpStatus.FORBIDDEN)// 상태코드 403 지정 (없으면 200)
    @ResponseBody
    @ExceptionHandler(Exception403.class)
    public String ex403(Exception403 e, HttpServletRequest request, Model model) {
        log.warn("=== 403 Forbidden error ===");
        log.warn("요청 URL : {}", request.getRequestURI());
        log.warn("권한 오류: {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        String msg = e.getMessage().replace("`","'"); //백틱이 메시지에 섞여 템플릿 백틱과 충돌하는 것 방지

        String msg2 = """
                <script>
                    alert(`%s`);
                    history.back();
                </script>
                """.formatted(msg);
        return msg2;
    }

    @ExceptionHandler(Exception404.class)
    public String ex404(Exception404 e, HttpServletRequest request, Model model) {
        log.warn("=== 404 Not Found error ===");
        log.info("요청 URL : {}", request.getRequestURI());
        log.info("찾을 수 없음 오류: {}", e.getMessage());
        log.info("예외 클래스 : {}", e.getClass().getSimpleName());

        model.addAttribute("msg",e.getMessage());
        return "err/404";
    }

    @ExceptionHandler(Exception500.class)
    public String ex500(Exception500 e, HttpServletRequest request, Model model) {
        log.warn("=== 500 Internal Server Error error ===");
        log.warn("요청 URL : {}", request.getRequestURI());
        log.warn("서버 오류: {}", e.getMessage());
        log.warn("스태 트레이스 : {}", e); //전체 스택 트레이스 포함

        model.addAttribute("msg","네트워크 일시적인 장애");
        return "err/500";
    }

    //기타 모든 runtimeException
    @ExceptionHandler(RuntimeException.class)
    public String ex500(RuntimeException e, HttpServletRequest request, Model model) {
        log.warn("=== 예상치 못한 런타인 에러발생 ===");
        log.warn("요청 URL : {}", request.getRequestURI());
        log.warn("에러 타입 : {}", e.getClass().getSimpleName());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("스태 트레이스 : {}", e); //전체 스택 트레이스 포함

        model.addAttribute("msg","시스템 오류 발생. 관리자에게 문의해주세요");
        return "err/500";
    }




}
