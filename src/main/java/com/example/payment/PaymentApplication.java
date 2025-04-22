
package com.example.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentApplication {
    public static void main(String[] args) {
        System.out.println(">>> App is starting..."); // 이게 뜨는지 확인
        SpringApplication.run(PaymentApplication.class, args);
    }
}
