package com.tenco.spring_blog.refund;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    // payment.id로 조회
    @Query("SELECT r FROM Refund r WHERE r.payment.id = :paymentPk")
    Optional<Refund> findByPaymentPk(@Param("paymentPk") Long paymentPk);

    // 환불 요청한 결제 PK 목록
    @Query("SELECT r.payment.id FROM Refund r WHERE r.user.id = :userId")
    List<Long> findPaymentPkListByUserId(@Param("userId") Long userId);

    @Query("""
                SELECT r FROM Refund r
                JOIN FETCH r.payment
                WHERE r.user.id = :userId
                ORDER BY r.id DESC
            """)
    List<Refund> findAllByUserIdJoinPayment(@Param("userId") Long userId);
}
