package com.tenco.spring_blog.purchase;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.board.BoardJpaRepository;
import com.tenco.spring_blog.board.BoardService;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import com.tenco.spring_blog.user.UserService;
import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseService {

    private  final BoardJpaRepository boardJpaRepository;
    private  final UserJpaRepository userJpaRepository;
    private final PurchaseJpaRepository purchaseJpaRepository;

    //유료 게시글 구매
    // 포인트 차감 + 구매내역 저장 하나의 트랜잭션
    @Transactional
    public void purchase(Long boardId, Long userId) {
        //1. 게시글 조회
        Board board =  boardJpaRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을수 없습니다"));

        //2. 유료 게시글인지 확인
        if (!board.isPremium()){
            throw new Exception400("유료 게시글이 아닙니다");
        }

        //3. 작성자는 자기글을 구매할수없음 (이미 열람가능)
        if (board.isOwner(userId)){
            throw new Exception400("자신이 작성한 게시글을 구매할 수 없습니다");
        }
        //4. 첫구매자인지 확인
        if (purchaseJpaRepository.existsByUserIdAndBoardId(userId,boardId)){
            throw new Exception400("이미 구매한 게시글입니다");
        }
        //5. 구매자 조회 구매자 포인트 확인및 차감
        User user = userJpaRepository.findById(userId)
                .orElseThrow(()->new Exception404("사용자를 찾을수 없습니다"));

        user.deductPoint(Board.PREMIUM_PRICE);
        //6, 구매 내역 저장
        Purchase purchase = Purchase.builder()
                .user(user)
                .board(board)
                .price(Board.PREMIUM_PRICE)
                .build();

        purchaseJpaRepository.save(purchase);

    }



}
