package com.tenco.spring_blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Controller
public class BoardController {

    // GET http://localhost:8080/board/list
    // http://localhost:8080/
    @GetMapping({"/","/board/list"})
    public  String lsit (Model model) {
        //뼈대용 임시 데이터
        model.addAttribute("boardList", List.of(
                Map.of("id",1, "title","첫번쨰글"),
                Map.of("id",2, "title","두번쨰글"),
                Map.of("id",3, "title","세번쨰글")
                ));
        return "board/list";
    }
    // GET http://localhost:8080/board/3
    @GetMapping("/board/{id}")
    public  String detail (@PathVariable(name = "id") Long id, Model model) {
        //뼈대용 임시 데이터
        model.addAttribute("board", sampleBoard(id));
        return "board/detail";
    }

    // GET http://localhost:8080/board/save
    @GetMapping("/board/save")
    public  String saveForm () {

        return "board/save-form";
    }


    // GET http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public  String updateForm (@PathVariable long id, Model model) {
        model.addAttribute("board", sampleBoard(id));
        return "board/update-form";
    }



    // TODO
    // 뼈대용 임시 게시글 한ㄴ개 (데이트 베이스 연결시 삭제 예정)
    private Map<String, Object> sampleBoard(Long id) {
        return Map.of("id",id, "title",id+"번쨰 글",
                "content","임시 내용 ..",
                "username", "김민수");
    }
}

