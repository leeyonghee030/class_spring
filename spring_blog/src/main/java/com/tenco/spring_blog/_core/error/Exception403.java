package com.tenco.spring_blog._core.error;

// 403번대 Forbidden 상화엥서 사용할 사용자 정의 예외 ㅡㅋㄹ래스
// RuntimeException 을 상속하여 언체크 예외로 만듦
public class Exception403 extends RuntimeException {
    //예외 메세지를 받을수있도록 String 파라미터 설계
    public Exception403(String msg) {
        super(msg); //부모 클래스 메시지 설정
    }


}
