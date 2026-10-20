package com.tenco.spring_blog.user;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

    //사용자의 역할 목록 (User 1 : UserRole N)
    // cascade,orphanRemoval
    // 두옵션은 부모쪽의 @onetoMany 컬랙샨에 붙인다 User가 List<UserRole>을
    // 가지고 있으므로 사용할수있다
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false)
    private List<UserRole> roles = new ArrayList<>();

    //가입경로 (DB에는 LOCAL, KAKAO 문자열로 저장)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'LOCAL'") // 문자열이므로 안에 작은 따음표 필수
    private OauthProvider provider;

    //id 와 createdAt은 자동으로 채워지므로 빌더에서 제외
    @Builder
    public User(String username, String password, String email, String profileImage, OauthProvider provider) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
        this.provider = (provider == null) ? OauthProvider.LOCAL : provider;
    }

    public void update(String password){
        this.password = password;
    }

    //프로필 이미지 변경 (null이면 삭제) -트렌잭샨 안에서 호출하면 더티 체킹 발생됨
    public void updateProfileImage(String profileImage){
        this.profileImage = profileImage;
    }

    //역할 추가 (회원가입시 기본 USER 부여 , 관리자는 직접지정)
    public void addRole(Role role) {
        //이미 가진 역할이면 추가하지않음 코드에서 1차방어
        if (hasRole(role)) {
            return;
        }
        this.roles.add(UserRole.builder().role(role).build());
    }

    // 해당 역할을 가지고있는지 확인
    public boolean hasRole(Role role) {
        for (UserRole u : this.roles) {
            if (u.getRole() == role) {
                return true;
            }
        }
        return false;
    }


    //로그인한 사용자의 관리자 여부 확인 메서드
    public boolean isAdmin() {
        return hasRole(Role.ADMIN); //고정값
    }

    public String getProfilePath() {
        if (this.profileImage == null){
            return null;
        }
        if(this.profileImage.startsWith("http")){
            return this.profileImage;
        }
        return "/images/"+this.profileImage;
    }


}
