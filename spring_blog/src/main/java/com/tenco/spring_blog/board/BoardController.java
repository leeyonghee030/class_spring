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
    private  final  BoardPersistRepository boardPersistRepository;

    // GET http://localhost:8080/board/list
    // http://localhost:8080/
    @GetMapping({"/","/board/list"})
    public  String list (Model model) {
        List<Board> boardList = boardPersistRepository.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }
    // GET http://localhost:8080/board/3
    @GetMapping("/board/{id}")
    public  String detail (@PathVariable(name = "id") Long id, Model model) {
//        Board boardEntity = boardPersistRepository.findById(id);
        Board boardEntity = boardPersistRepository.findByIdWithJPQL(id);
        if (boardEntity == null){
            //추후에 404에러 페이지를 만들어서 처리할 예정
            throw new RuntimeException("게시글을 찾을 수 없습니다 : " + id);
        }

        model.addAttribute("board", boardEntity);
        return "board/detail";
    }



    // GET http://localhost:8080/board/save
    @GetMapping("/board/save")
    public  String saveForm () {

        return "board/save-form";
    }

    //코드 추가
    // post http://localhost:8080/board/save
    @PostMapping("/board/save")
    //Spring 폼 데이터를 객체로 변환하는 과정 (데이터 바인딩 메커니즘)
    // 폼 데이터 바인딘: Spring이 HTTP 요펑 파라미터를 객체로 자동 변환
    public  String save (BoardRequest.SaveDto reqDto) {
        //1. dto  class에서 entity class 타임으로 변환
        Board board = Board.builder()
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .username(reqDto.getUsername())
                .build();
                //new Board(reqDto.getTitle(),reqDto.getContent(),reqDto.getUsername());
        Board boardEntity = boardPersistRepository.save(board);
        //아직안씀 예시로해봄

        return "redirect:/";
    }

    //<a href="/board/{{board.id}}/update" class="btn btn-warning me-1">수정</a>
    // GET http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public  String updateForm (@PathVariable long id, Model model) {
        Board board = boardPersistRepository.findById(id);
        model.addAttribute("board", board);
        return "board/update-form";
    }
    // post http://localhost:8080/board/1/update
    // 게시글 수정 요청 기능
    @PostMapping("/board/{id}/update")
    public  String update (@PathVariable long id,BoardRequest.updateDto reqDto) {
       reqDto.validate(); // 유효성 실패 (throw 던져짐)
        boardPersistRepository.updateById(id, reqDto);
        //PRG 패턴
        // /board/{id}
        return "redirect:/board/" +id; //리다이렉트 수정된 게시글 상세보기 화면이동
    }

    // 게시글 삭제
    //  /board/{{board.id}}/delete method="post
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id){
        boardPersistRepository.deleteById(id);
        //PRG 패턴사용
        return "redirect:/";
    }


}

