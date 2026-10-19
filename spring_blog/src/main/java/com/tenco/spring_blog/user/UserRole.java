package com.tenco.spring_blog.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


// 복합 유니크 제약
// - 한 사용자는 같은 역할을 한번만 가진다.
// columnNames 에는 자바 필드명이 아니라 DB컬럼명을 써야한다 주의
// 현재 user_id 는 이클래스에 필드에 없지만 User엔티티 츶에서 @joinColumn(name= user_id)를
// 설정하면 자동으로 만들어진다
@Table(name = "user_role_tb",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_role",
                        columnNames = {"user_id", "role"})
        })
@Entity
@Getter
@NoArgsConstructor
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder // id는 자동으로 채워지므로 빌더에서 제외

    public UserRole(Role role) {
        this.role = role;
    }


}
