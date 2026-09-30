package com.examlie.demo2.controller;


import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


// new 로 객체 만든 후 메소드 호출해야하는데
// IoC (제어에 역전 실행을 원래 개발자가하는데 실행의 제어권을 프레임로 권한을줌)
@RestController
public class MappingRestController {
    //주소 설계
    // GET -http://localhost:8080/hi
    @GetMapping("/hi")
    public String hi() {
        return "호구마 안녕하세요";
    }

    //2. 객체를 반환하는 응답을 만들어보자

    //주소 설계 :GET /user-info
    @GetMapping("/user-info")
    public Map<String, Object> userInfo() {
        //Map.of() 불변 객체를 만들어주는 함수
        // 스프링 프레임워크 내부에 있는 메세지 컨버터랑 녀석이 알아서 JSON 형식으로 반환해줌
        return  Map.of("name","김민수","age","20");
    }

    // 3. 경로 변수 : 특정 대상 하나를 가리킬떄
    /// GET - http://localhost:8080/users/101
    @GetMapping("/users/{userId}")
    public String findUser(@PathVariable  Long userId) {
        System.out.println("경로변수 확인 : " +userId );
        return userId + "번 사용자 조회 (DB는 나중)";
    }

    //4. 쿼리 파라미터 : 목록을 거르거나 옵션을 줄댸
    //GET http://localhost:8080/search?keyword=스프링&page=2
    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "1") Integer page) {
        return "검색어 : " + keyword + ", 페이지 : " +page;
    }

    //5. 폼 데이터 : 여러 값을 보통 DTO 객체를 설계해서 하나의 객체로 만들어 받습니다
    // 어노테이션 없이 파라미터에 적기만 하면 (클래스 이름 ) 요청의 이름과 같은
    // 필드에 스프링이 값을 넣어준다

    // POST  http://localhost:8080/users  (username = 김민수, email=a@naver,com)
    @PostMapping("/users")
    public String join(UserJoinDTO JoinDTO) {
            // 오는 값에대해서만 받고없으면 null값
        return JoinDTO.getUsername() + "님 가입 요청을 받음" + JoinDTO.getEmail();
    }



    @Data
    public static class UserJoinDTO {
        private  String username;
        private  String email;

    }

}
