package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog.payment.PaymentResponse;
import com.tenco.spring_blog.payment.PortOntApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.PrintWriter;


//환불 승인 처리 순서 관리 검사 포트윈 취소 응답 확인 db반영
//트랜젝션을 걸지않는다 : 포트원 응답을기다리는 동안 쓰기 트렌잭션을 열어두지않기위해
// DB작업은 트랜션이있는 RefundService에 맡긴다
@Slf4j
@Service
@RequiredArgsConstructor
public class PortOntRefundService {
    private final PortOntApiClient PortOntApiClient;
    private final RefundService refundService;

    public void approve(Long refundId){
        //승인 가능 여부 검사 (DB조회만)
         RefundResponse.ApproveDto approveDto = refundService.checkApprove(refundId);
        //포트원 결제 취소 요청 외부통신 트랜잭샨 밖완료
        PaymentResponse.PortOneCancelDto  portOneCancelDto;
        try {
            portOneCancelDto = PortOntApiClient.cancelPayment(approveDto.getPaymentId(), approveDto.getAmount());
        } catch (RestClientResponseException e) {
            //400.401,403,404,409 - 포트원이 오류 상태 코드로 응답
            throw new RuntimeException(e);
        } catch (RestClientException e) {
            //502 - 응답을 받지 못함 (연결실패,시간초과등
            throw new RuntimeException(e);
        }
        //취소 완료상태확인
        if (portOneCancelDto == null || portOneCancelDto.getCancellation() ==null
        || !"SUCCEEDED".equals(portOneCancelDto.getCancellation().getStatus())){
            throw new Exception400("결제취소가 완료되지 않았습니다. 잠시 후 포트원 콘솔에서 확인해 주세요");
        }
        //db반영 (포인트 차감, 결제 CANCELLED로 변경, refund APPROVED)
        refundService.approve(refundId);

    }

}
