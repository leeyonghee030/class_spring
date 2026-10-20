package com.tenco.spring_blog.sts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDto {

    private Integer userId;
    private Integer id; // 보낼떄는모음 저장시 응답시 할당
    private String body;
    private String title;

    @Builder
    public PostDto(Integer userId, String body, String title) {
        this.userId = userId;
        this.body = body;
        this.title = title;
    }
}
