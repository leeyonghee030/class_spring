package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    //의존성 설정, application.yaml의 spring.mail 설정으로 스프링부트가 만들어준 빈 객체이다
    private final JavaMailSender javaMailSender;

    //From 미지정시 OS 로그인 계정명(한글 등)으로 기본값이 생성되어 Gmail이 거부하므로 명시적으로 지정
    @Value("${spring.mail.username}")
    private String fromAddress;

    // recode 문법: JDK 16 정식으로 사용할 수있음
    // 클래스를 설계해준다 필드 + getter, equals, hashCode, toString을 가진 클래스를
    // 자바가 대신 만들어준다 , 상속 불가
    //직렬화 바이트배열로
    private record EmailCode(String code, LocalDateTime expiresAt)implements Serializable {

        boolean isExpired(){
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }

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
            helper.setText("<h3>인증번호는 [" + code + "] 입니다</h3>"+
                    "<p>" + Define.CODE_EXPIRE_MINUTES + "분 안에 입력해주세요 </p>",true);  //본문

            //1-3 발송 (실패하면)
            javaMailSender.send(message);

        } catch (Exception e) {
            throw new Exception500("메일 작성 중 오류가 발생했습니다");
        }


        //인증시간 + 유효시간 해서 세션넣기
        session.setAttribute(Define.EMAIL_CODE_PREFIX + email,
                new EmailCode(code,LocalDateTime.now().plusMinutes(Define.CODE_EXPIRE_MINUTES)));
        log.info("인증번호 발송 완료 - 이메일: {}", email); //인증번호 자체는 로그에 안남기는게 졸음


    }


    //2. 인증번호 확인 : 일치하면 true 반환
    public boolean verifyCode(String email, String code, HttpSession session) {
        String key = Define.EMAIL_CODE_PREFIX + email;
        EmailCode saveEmailCode = (EmailCode) session.getAttribute(key);
        if (saveEmailCode == null) {
            throw new Exception400("인증번호를 먼저 요청해주세요");
        }
        if (saveEmailCode.isExpired()){
            session.removeAttribute(key); // 만료된 인증번호는 세션에서 삭제
            throw new Exception400("인증번호가 만료되었습니다. 다시 요청해주세요");
        }
        if (!saveEmailCode.code.equals(code)){
            throw new Exception400("인증번호가 일치하지 않습니다");
        }


        // 사용한 인증번호는 삭제
        session.removeAttribute(key);

        //인증을 마치면 이메일을 세션에 기록해야함 (회원가입시 서버가 확인)- 인증된 이메일이 아닌 변조해서 던질수있음
        session.setAttribute(Define.VERIFIED_EMAIL,email);
        return true;
    }
}
