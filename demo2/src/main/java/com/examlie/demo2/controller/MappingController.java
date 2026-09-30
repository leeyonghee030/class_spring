package com.examlie.demo2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


// @Controller 리턴 타입에 반환한 타입이 문자열이라면 화면 파일(템플릿) 이름으로 해석합니다
@Controller // IoC 제어의 역전 -> 프레임워크가 어노테이션을 확인 후 메모리 공간에
// 객체를 미리 생성해둔다.
public class MappingController {

    //템플릿 엔진으로 머스태치를 사용하고 이 녀석은 html 파일로 렌더링이 된 후
    //    웹 서버가 클라이언트에게 응답을 한다.

    // GET - http://localhost:8080/home-page
    // templates/index.mustache 파일을 찾아서  HTML파일로 응답을 합니다 (확장자 보통 생략)
    @GetMapping("/home-page")
    public String homePage() {
        return "index";
    }

}
