
package com.example.payment;

import ch.qos.logback.core.net.SyslogOutputStream;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;


//@RestController
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {



    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${toss.test-secret-key}")
    private String secretKey;

    @GetMapping("/payment")
    public String paymentForm() {
        return "payment";
    }

    @RequestMapping(value = "/request", method = {RequestMethod.GET, RequestMethod.POST})
    public String requestPayment(@ModelAttribute PaymentRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(secretKey, "");

        headers.setContentType(MediaType.APPLICATION_JSON);
        System.out.println(">>> App is starting..."); // 이게 뜨는지 확인

        Map<String, Object> body = new HashMap<>();
        body.put("amount", request.getAmount());
        body.put("orderId", request.getOrderId());
        body.put("orderName", request.getOrderName());
        body.put("customerName", request.getCustomerName());
        body.put("successUrl", "http://localhost:8080/payment/success");
        body.put("failUrl", "http://localhost:8080/payment/fail");
        System.out.println(">>> App is starting...2d"); // 이게 뜨는지 확인

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        System.out.println(">>> App is starting...3d"); // 이게 뜨는지 확인

        String tossUrl = "https://api.tosspayments.com/v1/payments";
//        return restTemplate.postForEntity(tossUrl, entity, String.class);
        ResponseEntity<String> response = restTemplate.postForEntity(tossUrl, entity, String.class);

        System.out.println("------------------------"+response.getBody());

        try {
            // JSON 응답에서 checkoutPage URL만 추출해서 리다이렉트
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());
            String checkoutUrl = json.get("checkoutPage").asText();
            System.out.println(">>>>>>>>>>>"+checkoutUrl+">>>>>>>>>>>");
            return "redirect:" + checkoutUrl;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "redirect:/paymentFail";  // 또는 에러 처리 페이지로
        }
    }

    @GetMapping("/fail")
    public String paymentFail() {
        return "paymentFail"; // templates/paymentFail.html
    }
}
