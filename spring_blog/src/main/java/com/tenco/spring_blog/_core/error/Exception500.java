package com.tenco.spring_blog._core.error;

// 500번대 Internal Server Error 상화엥서 사용할 사용자 정의 예외 ㅡㅋㄹ래스
// RuntimeException 을 상속하여 언체크 예외로 만듦
public class Exception500 extends RuntimeException {
    //예외 메세지를 받을수있도록 String 파라미터 설계
    public Exception500(String msg) {
        super(msg); //부모 클래스 메시지 설정
    }
}
