package com.example.demo3.controller;

import com.example.demo3.models.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller // html IOC
public class MustacheTestController2 {


    @GetMapping("/mustache-practice")
    public String practice(Model model) {
        // 1. 기본 출력
        model.addAttribute("username", "이순신");
        model.addAttribute("point", 5000);

        // 2. 조건 및 반전
        model.addAttribute("isVip", true);
        model.addAttribute("warningMsg", null); // null 데이터
        model.addAttribute("items", List.of()); // 빈 리스트

        // 3. 반복
        model.addAttribute("skills", List.of("Java", "Spring", "Mustache"));
        model.addAttribute("members", List.of(
                Map.of("name", "홍길동", "role", "USER"),
                Map.of("name", "관리자", "role", "ADMIN")
        ));

        // 4. 점(.) 표기법
        model.addAttribute("product", Map.of("name", "키보드", "price", 150000));

        // 5. 이스케이프
        model.addAttribute("tagData", "<mark>강조 텍스트</mark>");

        return "mustache-practice";
    }


}

