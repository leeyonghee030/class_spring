package com.tenco.spring_blog._core.config;

import com.tenco.spring_blog._core.interceptor.AdminInterceptor;
import com.tenco.spring_blog._core.interceptor.IpBlockInterceptor;
import com.tenco.spring_blog._core.interceptor.LoginInterceptor;
import com.tenco.spring_blog._core.interceptor.SessionUserModelInterceptor;
import com.tenco.spring_blog._core.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@RequiredArgsConstructor
@Configuration // 스프링 설정 클래스임을 표시
public class WebMvcConfig implements WebMvcConfigurer {


    private final LoginInterceptor loginInterceptor;
    private final IpBlockInterceptor ipBlockInterceptor;
    private  final SessionUserModelInterceptor sessionUserModelInterceptor;
    private  final AdminInterceptor adminInterceptor;
    //업로드 폴더의 파일을 /images/** 주소로 제공


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 압로드 폴더 경로를 file:///...혈식의 주소로 변환 (운영체제에 맞게 자동 변환 )
        String location = Paths.get(FileUtil.UPLOAD_DIR).toUri().toString();
        // 윈도우 기준 : C:\Users\사용자명\blog_uploads\

        // 폴더 위치는 반드시 / 로 끝나야 그안에 파일을 찾을수 있음
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }

    //내가 정의한 인터셉터를 설정 클래스 등록할수있다
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(ipBlockInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(sessionUserModelInterceptor)
                .addPathPatterns("/**");
        //LoginInterceptor를 시스템에 등록
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/user/**","/board/**","/reply/**","/api/sms/**")
                //인터셉터에서 제외할 URL 패턴을지정
                .excludePathPatterns("/board/list", "/board/{id:\\d+}");
                // \\d+는 정규 표현식으로 1개 이상의 숫자를 의미한다
                // 예: /board/1, /board/123 (상세보기)은 로그인 없이도 접근가능
                // /board/1/update 처럼 뒤에 경로가 더 붙으면 제외 대상이 아니게 된다
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }

    @Bean // IOC 처리됨
    //비밀번호 해싱 도구를 빈으로 등록 (필요한 곳에서 DI 할수있다)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
