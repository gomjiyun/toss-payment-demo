
package com.example.payment;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("/request")
    public ResponseEntity<String> requestPayment(@RequestBody PaymentRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(secretKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", request.getAmount());
        body.put("orderId", request.getOrderId());
        body.put("orderName", request.getOrderName());
        body.put("customerName", request.getCustomerName());
        body.put("successUrl", "http://localhost:8080/payment/success");
        body.put("failUrl", "http://localhost:8080/payment/fail");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String tossUrl = "https://api.tosspayments.com/v1/payments";
        return restTemplate.postForEntity(tossUrl, entity, String.class);
    }
}
