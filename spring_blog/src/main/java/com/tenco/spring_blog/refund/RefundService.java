package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.payment.Payment;
import com.tenco.spring_blog.payment.PaymentJpaRepository;
import com.tenco.spring_blog.payment.PaymentResponse;
import com.tenco.spring_blog.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true) //정합성을 위해 하는게좋다
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentJpaRepository paymentJpaRepository;

    public List<RefundResponse.ListDto> findAllByUserId(Long userId) {
        List<Refund> refundList = refundRepository.findAllByUserIdJoinPayment(userId);
        return refundList.stream()
                .map(RefundResponse.ListDto::new).toList();
    }

    public PaymentResponse.ListDto findRequestForm(Long paymentPk, Long userId) {
        Payment payment = checkRefundable(paymentPk, userId);
        return new PaymentResponse.ListDto(payment, true);
    }

    private Payment checkRefundable(Long paymentPk, Long userId) {
        // 1. payment 테이블에서 결제 내역 조회
        Payment payment = paymentJpaRepository.findById(paymentPk)
                .orElseThrow(() -> new Exception404("결제 내역을 조회할 수 없습니다"));

        // 2. 본인 확인
        if (!payment.isOwner(userId)) {
            throw new Exception403("환불 권한이 없습니다 (본인 결제만 환불 가능)");
        }

        // 3. 결제 완료 상태 확인
        if (!payment.isPaid()) {
            throw new Exception400("환불 요청 불가 (결제 완료 건만 환불 요청 가능)");
        }

        // 4. 환불 요청 중복 확인
        if (refundRepository.findByPaymentPk(paymentPk).isPresent()) {
            throw new Exception400("이미 환불 요청된 결제입니다");
        }

        return payment;
    }

    @Transactional
    public void save(RefundRequest.SaveDto saveDto, Long userId) {
        // 1. 폼 요청 화면처럼 검사를 한번 더 실행 (주소 직접 요청, 중복 제출 방지)
        Payment payment = checkRefundable(saveDto.getPaymentPk(), userId);

        // 2. 환불 요청 저장
        Refund refund = Refund.builder()
                .user(payment.getUser())
                .payment(payment)
                .reason(saveDto.getReason().trim())
                .build();

        refundRepository.save(refund);
    }


    public List<RefundResponse.AdminListDto> findAllForAdmin() {
        //관리자에서 전체조회
        List<Refund> refundList = refundRepository.findAllJoinUserAndPayment();
        return refundList.stream()
                .map(RefundResponse.AdminListDto::new).toList();
    }


    // SOLID 원칙
    //  단일 책임의 원칙
    //  개방과 폐쇄의 원칙
    //
    @Transactional
    public void reject(Long refundId, String rejectReason) {
//        1. 객체들고오가 조회
        Refund refund = refundRepository.findById(refundId).orElseThrow(() -> {
            throw new Exception404("환불 요청을 찾을수 없습니다");
        });
        //2. 이미 승인 ,거절된 요청은 굳이 처리하지않는다
        if (!refund.isPending()) {
            throw new Exception404("대기중인 환불 요청만 거절할 수 있습니다");
        }

        refund.reject(rejectReason); //더치체킹 상태값 변경
    }

    /**
     * 관리자 승인 1단계 : 승인 가능한지 검사 (db만 조회)
     *
     * @param refundId
     * @return : 포트원 결제 취소에 필요한 값 리턴
     */
    public RefundResponse.ApproveDto checkApprove(Long refundId) {
        Refund refund = refundRepository.findByIdJoinUserAndPayment(refundId).orElseThrow(() -> {
            throw new Exception404("환불된 결제건을 찾을수 없습니다");
        });
        if (!refund.isPending()) {
            throw new Exception400("대기  중인 환불 요청만 승인할 수 있습니다");
        }
        //결제부분 결제로되어있나?
        Payment payment = refund.getPayment();
        if (!payment.isPaid()) {
            throw new Exception400("결제 완료 상태만 환불 신청할 수 있습니다");
        }
        //요청자의 포인트 남은 여부확인
        User user = refund.getUser();
        if (user.getPoint() < payment.getAmount()) {
            throw new Exception400("포인트 잔액이 부족하여 환불 할 수 없습니다");
        }
        return new RefundResponse.ApproveDto(refund);

    }

    @Transactional
    public void approve(Long refundId) {
        Refund refund = refundRepository.findByIdJoinUserAndPayment(refundId).orElseThrow(() -> {
            throw new Exception404("환불된 결제건을 찾을수 없습니다");
        });

        //1-4 번사이에 다른 요청이 같은건을 처리햇을수 있어 한번더 확인
        if (!refund.isPending()) {
            throw new Exception400("대기 중인 환불 요청만 환불 승인 신청가능 ");
        }

        Payment payment = refund.getPayment();

        refund.getUser().deductPoint(payment.getAmount());
        payment.markCancelled();
        refund.approve();

    }
}
