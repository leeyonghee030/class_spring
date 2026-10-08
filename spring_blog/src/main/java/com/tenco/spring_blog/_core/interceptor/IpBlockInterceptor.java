package com.tenco.spring_blog._core.interceptor;


import com.tenco.spring_blog._core.error.Exception403;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

//조원들의 ip 차단
@Component
public class IpBlockInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String ip = request.getRemoteAddr().trim();
        if (ip.equals("192.168.5.18") || ip.equals("192.168.5.13") || ip.equals("192.168.5.17")) {
            throw new Exception403("차단된 ip입니다");
        }
        return true;
    }
}
