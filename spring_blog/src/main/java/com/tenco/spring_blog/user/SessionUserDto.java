package com.tenco.spring_blog.user;

import lombok.Getter;

/**
 * 세션 데이터 전용 DTO
 * - 로그인한 사용자를 식별하는데 필요한 값만 담는다
 * - 엔터티가 아니므로 값을 바꿔도 DB에 영향이없다
 * - 비밀번호를 담지않는다
 */
@Getter
public class SessionUserDto {
    private Long id;
    private String username;
    private String email;
    private boolean admin; //관리자 여부
    private String roleDisplay; //화면 표시용


    public SessionUserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.admin = user.isAdmin();
        this.roleDisplay = this.admin ? "ADMIN" : "USER";
    }
}
