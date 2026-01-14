package com.example.librarybot.dtapayment;

import lombok.Getter;
import lombok.Setter;

public class PaymentRequest {
    @Setter
    @Getter
    private Long amount; // Сумата
    @Setter
    @Getter
    private String currency; // Валутата (напр. "bgn")

    @Getter
    private String description; // За какво е плащането (напр. "Такса за забавяне")

    @Getter
    private String userEmail; // Имейл на клиента (полезно за разписката)

}
