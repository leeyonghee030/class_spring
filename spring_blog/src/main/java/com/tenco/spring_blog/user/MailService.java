package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog._core.util.MailUtil;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    //의존성 설정, application.yaml의 spring.mail 설정으로 스프링부트가 만들어준 빈 객체이다
    private final JavaMailSender javaMailSender;

    //From 미지정시 OS 로그인 계정명(한글 등)으로 기본값이 생성되어 Gmail이 거부하므로 명시적으로 지정
    @Value("${spring.mail.username}")
    private String fromAddress;

    //1. 인증번호 발송
    public void sendVerificationCode(String email, HttpSession session) {
        //1-1 6자리 인증번호 생성
        String code = MailUtil.generateCode();
        //1-2 메일 작성 (MimeMessage : HTML 본문, 첨부파일 등을 담을 수 있는 메일 형식)
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            //false : 첨부 파일 없음, UTF-8 한글꺠짐 방지
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress); //보내는 사람 이메일주소
            helper.setTo(email); //받는 사람 이메일주소
            helper.setSubject("[스프링블로그] 회원가입 인증번호");  //제목
            helper.setText("<h3>인증번호는 [" + code + "] 입니다</h3>",true);  //본문

            //1-3 발송 (실패하면)
            javaMailSender.send(message);

        } catch (Exception e) {
            throw new Exception500("메일 작성 중 오류가 발생했습니다");
        }


        session.setAttribute(Define.EMAIL_CODE_PREFIX + email, code);
        log.info("인증번호 발송 완료 - 이메일: {}", email); //인증번호 자체는 로그에 안남기는게 졸음


    }


    //2. 인증번호 확인 : 일치하면 true 반환
    public boolean verifyCode(String email, String code, HttpSession session) {
        String savedCode = (String) session.getAttribute(Define.EMAIL_CODE_PREFIX + email);
        if (savedCode == null || !savedCode.equals(code)){
            return false;
        }
        // 사용한 인증번호는 삭제
        session.removeAttribute(Define.EMAIL_CODE_PREFIX+email);

        //인증을 마치면 이메일을 세션에 기록해야함 (회원가입시 서버가 확인)- 인증된 이메일이 아닌 변조해서 던질수있음
        session.setAttribute(Define.VERIFIED_EMAIL,email);
        return true;
    }
}
