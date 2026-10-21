package com.tenco.spring_blog._core.util;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

//@Component <- 안할예정
// 내부에 이 클래스는 상태 값을 가지지 않게 한다
// FileUtil은  상태도 없고 다른 빈에 의존을 하지않는 순수한 도구 함수모음이라
// Static 메서드로 활용할 예정
@Slf4j
public class FileUtil {

    // 1. 업로드 폴더: 사용자 홈 폴더 아래 blog_uploads (운영 체제와 관계없이 동작)
    public static final String UPLOAD_DIR =
            Paths.get(System.getProperty("user.home"), "blog_uploads").toString();

    // 2. 허용하는 이미지 형식과 저장할 확장자 설정
    private static final Map<String, String> IMAGE_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    // 3. 이미지 저장 : 저장할 파일 이름을 반환 (파일이 없으면 null)
    public static String saveImage(MultipartFile file) {
        // 1. 파일저장은 선택 사항으로 결정할 예정
        if (file == null || file.isEmpty()) {
            return null;
        }
        // 2. 허용한 이미지 형식인지 확인
        if (!isImageFile(file)) {
            throw new Exception400("이미지 파일은(JPG, PNG, GIF, WEBP)만 업로드가능 ");
        }
        // 3. 겹치치 않은 새 파일 이름 만들기 (UUID + 확장자)
        String extension = IMAGE_TYPES.get(file.getContentType());
        String savedFilename = UUID.randomUUID() + extension;

        // 4.폴더가 없으면 만들고, 파일저장
        Path uploadPath = Paths.get(UPLOAD_DIR);
        try {
            Files.createDirectories(uploadPath); // 이미 경로가 있으면 아무 일도 하지않음
            file.transferTo(uploadPath.resolve(savedFilename)); // 폴더 경로 + 파일 이름 (파일 생성)
        } catch (IOException e) {
            log.error("파일 저정 실패 {}", e.getMessage());
            throw new Exception500("파일 저장 중 오류가 발생했습니다");
        }

        return savedFilename;
    }

    // 파일 삭제 ( 파일 이름이 없거나 파일이 이미 없으면 아무 일도 하지 않음)
    public static void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        // 카카오/구글 로그인 사용자의 프로필 사진은 외부 URL이라 서버에 실제 파일이 없다
        // (경로에 ':'가 있어 Paths.get()에서 InvalidPathException이 나므로 미리 건너뜀)
        if (filename.startsWith("http")) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(UPLOAD_DIR,filename));
        } catch (IOException e) {
            // 파일 삭제에 실패해도 사용자 요청은 계속 진행 로드만 남김
            log.warn("파일 삭제 실패 : {}", e.getMessage());
        }
    }

    // 편의 메서드: 허용한 이미지 형시인지 확인
    public static boolean isImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false; // 형식 정보가 없으면 허용하지 않음
        }
        log.info("업로드된 파일 contentType = [{}], originalFilename = [{}]",
                file.getContentType(), file.getOriginalFilename());
        return IMAGE_TYPES.containsKey(file.getContentType());

    }


}
