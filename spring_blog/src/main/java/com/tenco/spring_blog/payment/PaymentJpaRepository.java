package com.tenco.spring_blog.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment,Long> {

    //추후 필요한 부분 추가예정

    @Query("select p from Payment p where p.paymentId = :paymentId")
    Optional<Payment> findByPaymentId(@Param("paymentId") String paymentId);

    @Query("""
    select case when count(p) > 0 then true else false end
    from Payment p where p.paymentId = :paymentId
""")
    boolean existsByPaymentId(@Param("paymentId") String paymentId);


    // 결제 내역 조회 (로그인 사용자)
    @Query("""
                SELECT p FROM Payment p 
                WHERE p.user.id = :userId
                AND p.status IN :statusList 
                ORDER BY p.id DESC 
        """)
    List<Payment> findAllByUserIdAndStatusList(@Param("userId") Long userId,
                                               @Param("statusList") List<PaymentStatus> statusList );
}
