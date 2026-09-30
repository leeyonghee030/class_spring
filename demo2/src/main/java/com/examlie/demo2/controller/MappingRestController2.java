package com.examlie.demo2.controller;


import ch.qos.logback.classic.spi.STEUtil;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


// new 로 객체 만든 후 메소드 호출해야하는데
// IoC (제어에 역전 실행을 원래 개발자가하는데 실행의 제어권을 프레임로 권한을줌)
@RestController
public class MappingRestController2 {
    //주소 설계
    // GET -http://localhost:8080/hi
    @GetMapping("/hi")
    public String hi() {
        return "안녕하세요";
    }

    //2. 객체를 반환하는 응답을 만들어보자

    //주소 설계 :GET /user-info
    @GetMapping("/user-info")
    public Map<String, Object> userInfo() {
        return Map.of("name","김민스");
    }

    // 3. 경로 변수 : 특정 대상 하나를 가리킬떄
    /// GET - http://localhost:8080/users/101
    @GetMapping("/users/{userId}")
    public String findUser (@PathVariable Long userId) {
        return userId + "번 사용자 조회";
    }


    //4. 쿼리 파라미터 : 목록을 거르거나 옵션을 줄댸
    //GET http://localhost:8080/search?keyword=스프링&page=2
    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "1") Integer page) {
        return "검색어 : " +keyword + ", 페이지 : " + page;
    }


    //5. 폼 데이터 : 여러 값을 보통 DTO 객체를 설계해서 하나의 객체로 만들어 받습니다
    // 어노테이션 없이 파라미터에 적기만 하면 (클래스 이름 ) 요청의 이름과 같은
    // 필드에 스프링이 값을 넣어준다

    // POST  http://localhost:8080/users  (username = 김민수, email=a@naver,com)
    @PostMapping("/users")
    public String join(UserJoinDTO joinDTO) {
        return joinDTO.getUsername() +"님 가입 요청을 받음 " +joinDTO.getEmail();
    }




    @Data
    public static class UserJoinDTO {
        private  String username;
        private  String email;

    }

}
