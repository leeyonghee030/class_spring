package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.payment.Payment;
import com.tenco.spring_blog.payment.PaymentJpaRepository;
import com.tenco.spring_blog.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
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
}
