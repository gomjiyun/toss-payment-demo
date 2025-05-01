
package com.example.payment;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

//ObjecgMapper
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;

//responsentity
import org.springframework.http.ResponseEntity;


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

        String tossUrl = "https://api.tosspayments.com/v1/payments";
//        return restTemplate.postForEntity(tossUrl, entity, String.class);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tossUrl, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());

            String redirectUrl = json.path("checkoutPage").asText(); // 성공 시
            return "redirect:" + redirectUrl;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println("HTTP 오류 발생: " + e.getStatusCode());
            System.out.println("응답 내용: " + e.getResponseBodyAsString());

            // Toss 응답 본문 파싱해서 failUrl 추출
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode errorJson = mapper.readTree(e.getResponseBodyAsString());
                String failUrl = errorJson.path("error").path("data").path("failUrl").asText();
                System.out.println("1111111");
                if (failUrl != null && !failUrl.isEmpty()) {
                    return "redirect:" + failUrl;
                }
            } catch (JsonProcessingException parseError) {
                System.out.println("22222222");
                parseError.printStackTrace();
            }

            System.out.println("333333");
            // failUrl도 없거나 JSON 파싱 실패한 경우 기본 에러 페이지로
            return "redirect:/paymentFail";
        } catch (JsonMappingException e) {
            System.out.println("44444");
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            System.out.println("555555");
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/fail")
    public String paymentFail() {
        return "/payment/paymentFail"; // templates/paymentFail.html
    }
}
