package com.tenco.spring_blog.payment;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


//포트원 서버와 http 통신망 담당하는 클래스 (포트원 설정값도 이곳에서만 관리)

@Getter
@Slf4j
@Component
public class PortOntApiClient {

    @Value("${portone.store-id}")
    private String storeId;
    @Value("${portone.channel-key}")
    private String channelKey;
    @Value("${portone.api-secret}")
    private String apiSecret;

    private static final String PAYMENT_URL = "https://api.portone.io/payments";

    //여러 요청에서 함께 써도 안전하므로 하나만 만들어재사용
    private final RestTemplate restTemplate = new RestTemplate();

    //포트원 서버에 결제 단건 조회 (GET,)
    //get header 설정
    public PaymentResponse.PortOnePayment getPayment(String paymentId) {

        HttpHeaders headers = new HttpHeaders();
        //반드시 공백 한칸
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret);

        //GET 요청 이라 바디없이 헤더만담는다
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaymentResponse.PortOnePayment> response = restTemplate.exchange(
                PAYMENT_URL + "/" + paymentId,
                HttpMethod.GET,
                request,
                PaymentResponse.PortOnePayment.class
        );
        return  response.getBody();
    }


}
