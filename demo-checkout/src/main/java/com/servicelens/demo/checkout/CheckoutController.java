package com.servicelens.demo.checkout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/internal")
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final RestClient paymentClient;

    public CheckoutController(RestClient.Builder restClientBuilder,
                              @Value("${demo.payment.base-url}") String paymentBaseUrl) {
        this.paymentClient = restClientBuilder.baseUrl(paymentBaseUrl).build();
    }

    @GetMapping("/checkout")
    public String checkout(
            @RequestParam(defaultValue = "false") boolean failPayment,
            @RequestParam(defaultValue = "0") long paymentDelayMs) {
        log.info("Checkout calling payment");
        String body = paymentClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/pay")
                        .queryParam("fail", failPayment)
                        .queryParam("delayMs", paymentDelayMs)
                        .build())
                .retrieve()
                .body(String.class);
        return "checkout-ok:" + body;
    }
}
