package com.tenco.spring_blog.user;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import java.sql.Timestamp;

@Getter
@NoArgsConstructor
@Table(name = "user_tb")
@Entity
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    @Column(length = 100)
    @Setter
    private String password;
    //같은 사용자명은 두번 가입 할수없도록 유니크제약
    @Column(unique = true)
    private String email;

    @CreationTimestamp // now()
    private Timestamp createdAt;

    //프로필 이미지 파일 이름 (선택가능이므로 null 가능 )
    private String profileImage;

    //id 와 createdAt은 자동으로 채워지므로 빌더에서 제외
    @Builder
    public User(String username, String password, String email, String profileImage) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
    }

    public void update(String password){
        this.password = password;
    }

    //프로필 이미지 변경 (null이면 삭제) -트렌잭샨 안에서 호출하면 더티 체킹 발생됨
    public void updateProfileImage(String profileImage){
        this.profileImage = profileImage;
    }




}
