package com.tenco.spring_blog.sms;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SmsController {

    @GetMapping("/user/sms")
    public String getVerificationStatus() {
        return "user/phone-auth";
    }
}
