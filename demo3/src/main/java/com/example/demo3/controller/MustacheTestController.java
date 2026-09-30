package com.example.demo3.controller;

import com.example.demo3.models.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller // html IOC
public class MustacheTestController {


    //GET http://localhost:8080/mustache-test
    @GetMapping("mustache-test")
    public String mustacheTest(Model model) {
           // Model : 화면에 넘길 값을 담는 상자, 이름으로 넣고 템플릿에서 같은 이름으로 꺼낼수있다
        //1. 갑 출력
        model.addAttribute("stringValue","안녕하세요");
        model.addAttribute("intValue",1234);

        //2. 조건 센션, 반전센셕
        model.addAttribute("isLogin",true);
        model.addAttribute("notice","공지사항이 있습니다");
        model.addAttribute("emptyList", List.of());
        //

        // 3. 반복
        List list = new ArrayList<>();
        list.add("사과");
        list.add("배");
        list.add("포도");
//        model.addAttribute("truits", list);
        model.addAttribute("fruits", List.of("사과","배","포도"));
        model.addAttribute("users", List.of(
                new UserDTO("이구구",23),
                new UserDTO("이창창",24),
                new UserDTO("이만만",25)
        ));

        //4. 점 (.)으로 안쪽 값 꺼내기
        model.addAttribute("info", Map.of("job","개발자","city","부산"));


        // 5. 이스케이프 비교
        model.addAttribute("htmlContent", "<b>굵은 글씨</b>");
        return "mustache-test";
    }
    //http://localhost:8080/layout-test
    @GetMapping("/layout-test")
    public String layoutTest() {
        return "layout-test";
    }




}

