package com.tenco.spring_blog.sts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
//외부 서버에서는 우리서버에 불필요한 내용까지 많은 데이터를 전달한다
// 불필요한 필드 즉 선언이 안된 우리코드 데이터 무시해 (안전상에 이유로 선언을 권장)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TodoDto {
    //Json의 key 이름과 필드 이름이 같아야 값이 들어온다
    private Integer userId;
    private Integer id;
    private String title;
    private boolean completed;


}
