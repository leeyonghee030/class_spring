package com.tenco.spring_blog.sts;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.util.List;

@RestController // @controller + @ResponsBody
public class RestTemplateController {

    //테스트 주소 - http://localhost:8080/practice/todos/1
    @GetMapping("/practice/todos/{id}")
    public ResponseEntity<?> getTodoAsString(@PathVariable(name = "id") Integer id) {

        //요청할 외부 API주소
        String url = "https://jsonplaceholder.typicode.com/todos/" + id;

        //2. http 요청을 보내는 도구생성
        RestTemplate restTemplate = new RestTemplate();

        //3. 요청헤더 - 응답을 JSON으로 줘
        HttpHeaders headers = new HttpHeaders();
        // 추후 외부 API 사용시 헤더에 어떻게 던져라 양식이 문서에 나와있음 보고 맞춰서 만들어 줘야함
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        //4. 요청 꾸러미 (HTTPEntity) = 헤더 + 바디 (http 요청메세지 헤더와 바디 만들어준다)
        HttpEntity request = new HttpEntity<>(headers);
        //get 요청 자원의 요청 바디가 없음

//        5. 요청 보내기 exchange(주소, 메서드, HTTP메서지, 응답바디 타입)
         ResponseEntity<TodoDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                TodoDto.class
        );

         //6. 응답 확인
        System.out.println("상태 코드 확인 : " + response.getStatusCode());
        System.out.println("응답 헤더 확인 : " + response.getHeaders());
        System.out.println("바디 내용 확인 : " + response.getBody());
        //7. 응답 DTO 형태구축
        System.out.println("userId : " + response.getBody().getUserId());
        System.out.println("id : " + response.getBody().getId());
        System.out.println("title : " + response.getBody().getTitle());
        System.out.println("completed : " + response.getBody().isCompleted());


        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }

    //브라우저에서 우리 서버 요청
    // 테스트 주소     //테스트 주소 - http://localhost:8080/practice/posts
    @GetMapping("/practice/posts")
    public ResponseEntity<?> createPost() {

        //요청할 외부 API주소
        String url = "https://jsonplaceholder.typicode.com/posts";

        //2. http 요청을 보내는 도구생성
        RestTemplate restTemplate = new RestTemplate();

        //3. 요청헤더 - 응답을 JSON으로 줘
        HttpHeaders headers = new HttpHeaders();
        // 추후 외부 API 사용시 헤더에 어떻게 던져라 양식이 문서에 나와있음 보고 맞춰서 만들어 줘야함
        // ContentType : 보내는 바디는 JSON 이다
        // Accept      : 응답은 JSON 으로 주세요
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        //4. 보낼 데이터 구성
        PostDto postDto = PostDto.builder()
                .userId(99)
                .title("오늘은 s to s 배움")
                .body("오호호호홓홓")
                .build();

        //5. 요청 꾸러미 (HTTPEntity) =  바디 + 헤더  (http 요청메세지 헤더와 바디 만들어준다)
        HttpEntity request = new HttpEntity<>(postDto, headers);
        //get 요청 자원의 요청 바디가 없음

        try {

            //        6. 요청 보내기 exchange(주소, 메서드, HTTP메서지, 응답바디 타입)
            ResponseEntity<PostDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    PostDto.class
            );

            // DTO - 하나로 요청 , 응답
            // 요청 DTO 응답 DTO 설계가 관례

            System.out.println(response.getStatusCode());
            System.out.println(response.getHeaders());
            System.out.println(response.getBody());
            return ResponseEntity.ok().body(response.getBody());
        } catch (HttpClientErrorException e) {
            //외부 서버가 보낸 상태 코드와 오루내용을 확인할수있다
            return ResponseEntity.status(e.getStatusCode()).body("외부 API  요청에 실패했습니다");

        }
    }



}
