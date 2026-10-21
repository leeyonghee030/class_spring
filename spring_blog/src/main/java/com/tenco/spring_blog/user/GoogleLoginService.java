package com.tenco.spring_blog.user;


import com.tenco.spring_blog._core.error.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleLoginService {

    private final UserService userService;
    private final GoogleApiClient googleApiClient;


    // 구글 로그인 화면주소
    public String getAuthorizeUrl(String state) {
        return googleApiClient.getAuthorizeUrl(state);
    }

    // 카카오 로그인 : 인가 코드 -> 토큰 -> 사용자 정보 -> 가입 또는 로그인
    public SessionUserDto login(String code) {

        if (code == null) {
            throw new UnauthorizedException("구글 로그인이 취소 되었습니다");
        }
        try {
            // 1. 인가 코드로 액세스 토큰 발급
            UserResponse.OauthToken oauthToken = googleApiClient.requestAccessToken(code);

            if (oauthToken == null || oauthToken.getAccessToken() == null) {
                throw new UnauthorizedException("구글 로그인중 실패하였습니다");
            }
            // 토큰으로 사용자 정보 조회
            UserResponse.GoogleUserInfo googleUserInfo  = googleApiClient.requestProfile(oauthToken.getAccessToken());
            //3. 자동 회원 가입 또는 로그인 (우리 DB 트랜잭션은 UserService 에서 )

            return userService.googleLogin(googleUserInfo);

        } catch (HttpClientErrorException e) {
            // 카카오 서버에서 4xx 응답한 경우 (인가 코드 재사용, 키오류등)
            log.warn("카카오 API 오류 : {}", e.getResponseBodyAsString());
            throw new UnauthorizedException("카카오 로그인에 실패했습니다 다시 시도해주세요");
        } catch (ResourceAccessException e) {
            // 카카오 서버에 연결하지 못한 경우 (네트워크 오류 + 시간초과등)
            log.warn("카카오 서버에 연결 실패 : {}",e.getMessage());
            throw new UnauthorizedException("카카오 서버에 연결할 수 없습니다 잠시 후 다시 시도해 주세요 ");
        }
    }


}
