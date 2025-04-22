
package com.example.payment;

import lombok.Data;

@Data
public class PaymentRequestDto {
    private String orderId;
    private String orderName;
    private String customerName;
    private int amount;
}
