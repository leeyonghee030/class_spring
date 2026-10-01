package com.tenco.spring_blog.board;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor // DI 처리
public class BoardController {

    private  final  BoardNativeRepository boardNativeRepository;

    // GET http://localhost:8080/board/list
    // http://localhost:8080/
    @GetMapping({"/","/board/list"})
    public  String list (Model model) {
        List<Board> boardList = boardNativeRepository.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }
    // GET http://localhost:8080/board/3
    @GetMapping("/board/{id}")
    public  String detail (@PathVariable(name = "id") Long id, Model model) {
        Board board = boardNativeRepository.findById(id);
        if (board == null){
            return "redirect:/";
        }

        model.addAttribute("board", board);
        return "board/detail";
    }



    // GET http://localhost:8080/board/save
    @GetMapping("/board/save")
    public  String saveForm () {

        return "board/save-form";
    }

    //코드 추가
    // post http://localhost:8080/board/save
    // 스프링 부트의 데이터 기본 파싱 전략 key=value
    //name 속성 기존으로 값을 추출할수있다
    @PostMapping("/board/save")
    public  String save (@RequestParam("username") String username,
                         @RequestParam("title") String title,
                         @RequestParam("content") String content) {
        // 폼의 name 속성과 매개변수명이 일치하면 자동으로 값이 바인딩 됨
        // 즉 name="title" -> String title로 자동 매핑
        log.info("username : {}", username);
        log.info("title : {}", title);
        log.info("content : {}", content);

        //DAO  객체에게 데이터를 전달후 저장하는 일을 위임
        boardNativeRepository.save(title,content,username);

        // redirect: 저장 후 메인 페이지로 이동
        // POST 요청후 redirect 로 PRG (POST-redirect-GET) 패턴규칙
        return "redirect:/";
    }

    //<a href="/board/{{board.id}}/update" class="btn btn-warning me-1">수정</a>
    // GET http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public  String updateForm (@PathVariable long id, Model model) {
        Board board = boardNativeRepository.findById(id);
        model.addAttribute("board", board);
        return "board/update-form";
    }
    // post http://localhost:8080/board/1/update
    // 게시글 수정 요청 기능
    @PostMapping("/board/{id}/update")
    public  String update (@PathVariable long id,
                           @RequestParam(name = "title") String title,
                           @RequestParam(name = "content") String content) {
       boardNativeRepository.updateByid(title, content, id);
        //PRG 패턴
        // /board/{id}
        return "redirect:/board/" +id; //리다이렉트 수정된 게시글 상세보기 화면이동
    }

    // 게시글 삭제
    //  /board/{{board.id}}/delete" method="post"
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id){
        boardNativeRepository.deleteById(id);
        //PRG 패턴사용
        return "redirect:/";
    }


}

