package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.StyledEditorKit;


public class UserRequest {

    //이메일 인증 요청 DTO (발송 롹인 API에서 함께사용)
    @Data
    public static class EmailCheckDto{

        private String email; // 1번쨰 요청시 사용
        private String code; // 인증번호 (2번때 요청시 사용)

        public void validate() {
            if (email == null || email.trim().isEmpty()) {
                throw new Exception400("이메일을 입력해주세요");
            }
            if (!email.contains("@")){
                throw new Exception400("올바른 이메일 형식이 아닙니다");
            }
        }
        //인증번호 확인 API에서 추가로 호출할 예정
        public void validateCode() {
            validate();
            if (code== null || code.trim().isEmpty()) {
                throw new Exception400("인증번호를 입력해주세요");
            }
        }
    }


    //회원 정보 수정 DTO
    @Data
    public static class UpdateDto {
        @ToString.Exclude
        private String password;
        private MultipartFile profileImage;

        //비밀번호를 입력했는지 확인  (null."" 공백만 입력 ->모두 입략안함)
        public boolean hasPassword() {
            // 비밀번호 입력시 -> true 반환
            return password != null && !password.isBlank();
        }
        // 새프로필 사진 골랐는지
        public boolean hasProfileImage() {
            // 새파일 선택 입력시 -> true 반환
            return profileImage != null && !profileImage.isEmpty();
        }


        public void validate() {
            if (!hasPassword() && !hasProfileImage()) {
                throw new Exception400("변경할 비밀번호나 프로필 사진을 입력해주세요");
            }
            //필요하다면 길이수 제한 ,특수문자 포함여부 정규식
            if (hasPassword() && password.length() < 4) {
                throw new Exception400("비밀번호는 4글자 이상이여야 합니다");
            }
        }



    }

    // 회원가입용 DTO
    @Data
    public static class JoinDto {
        private String username;
        @ToString.Exclude
        private String password;
        private String email;
        private MultipartFile profileImage; //프로필 이미지


        //회원가입시 데이터 검증 메서드
        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수입니다");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호는 필수입니다");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new Exception400("이메일은 필수입니다");
            }
            //간단하게 이메일 형식 검증
            if(!email.contains("@")){
                throw new Exception400("올바른 이메일 형식이 아닙니다");
            }
        }
        //DTO 에서 User 엔터디로 변환하는 메서드
        //계층 간 데이터 변환을 명확하게 분리하는것이 좋다



        public User toEntity(String profileImageFilename, String passwordEncode) {
            return  User.builder()
                    .username(username)
                    .password(passwordEncode)
                    .email(email)
                    .profileImage(profileImageFilename)
                    .build();
        }
    }

    @Data
    public static class LoginDto {
        private String username;
        @ToString.Exclude
        private String password;


        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수입니다");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호는 필수입니다");
            }
        }

    }


}
