package com.tenco.spring_blog.payment;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
@RequiredArgsConstructor
public class PortOnePaymentService {

    private final PaymentService paymentService;
    private final PortOntApiClient portOntApiClient;


    public PaymentResponse.PrepareDto prepare(Long sessionUserid, Integer amount) {
        String paymentId = paymentService.prepare(sessionUserid, amount);

        return PaymentResponse.PrepareDto.builder()
                .paymentId(paymentId)
                .amount(amount)
                .storeId(portOntApiClient.getStoreId())
                .channelKey(portOntApiClient.getChannelKey())
                .build();
    }

    // 3단계 포트원 조회 (트랜잭션 밖) -> 검증 (포트원 서버) -> 충전 처리(트랜잭션 안)
    public PaymentResponse.CompleteDto complete(Long sessionUserid, String paymentId) {
        PaymentResponse.PortOnePayment portOnePayment;
        //1. 포트원 서버에 실제 결제 조회 (외부 통신 sts)
        try {
            portOnePayment = portOntApiClient.getPayment(paymentId);

        } catch (HttpClientErrorException e) {
            //포트원 서버 4xx 응답 (없는 결제건 ,api시크릿키 오류)
            throw new Exception400("결제 서버에 연결할 수 없습니다");
        } catch (ResourceAccessException e) {
            //포트원 서버 연결실패 (네트워크 오류 , 시간초가
            throw new Exception500("결제 서버에 연결할 수 없습니다");
        }

        if (portOnePayment == null) {
            throw new Exception400("결제 정보를 확인할 수 없습니다");
        }

        //2. 검증 (포트원 응답 데이터, 결제 DB정보 비교 검증) -> 포인트 충전
        return paymentService.complete(sessionUserid, paymentId, portOnePayment);
    }
}
