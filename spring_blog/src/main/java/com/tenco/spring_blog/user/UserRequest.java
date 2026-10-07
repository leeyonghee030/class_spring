package com.tenco.spring_blog.user;

import lombok.Data;


public class UserRequest {

    //회원 정보 수정 DTO
    @Data
    public static class UpdateDto {
        private String password;

        public void validate() {
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호는 필수입니다");
            }
            //필요하다면 길이수 제한 ,특수문자 포함여부 정규식
            if (password.length() < 4) {
                throw new IllegalArgumentException("비밀번호는 4글자 이상이여야 합니다");
            }
        }



    }

    // 회원가입용 DTO
    @Data
    public static class JoinDto {
        private String username;
        private String password;
        private String email;

        //회원가입시 데이터 검증 메서드
        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명은 필수입니다");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호는 필수입니다");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("이메일은 필수입니다");
            }
            //간단하게 이메일 형식 검증
            if(!email.contains("@")){
                throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
            }
        }
        //DTO 에서 User 엔터디로 변환하는 메서드
        //계층 간 데이터 변환을 명확하게 분리하는것이 좋다



        public User toEntity() {
            return  User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .build();
        }
    }

    @Data
    public static class LoginDto {
        private String username;
        private String password;


        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명은 필수입니다");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호는 필수입니다");
            }
        }

    }


}
