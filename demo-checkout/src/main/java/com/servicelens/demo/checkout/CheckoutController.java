package com.servicelens.demo.checkout;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/internal")
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final RestClient http;
    private final String paymentBaseUrl;

    public CheckoutController(RestClient.Builder restClientBuilder,
                              @Value("${demo.payment.base-url}") String paymentBaseUrl) {
        this.http = restClientBuilder.build();
        this.paymentBaseUrl = paymentBaseUrl;
    }

    @GetMapping("/checkout")
    public String checkout(
            @RequestParam(defaultValue = "false") boolean failPayment,
            @RequestParam(defaultValue = "0") long paymentDelayMs) {
        log.info("Checkout calling payment");
        URI uri = UriComponentsBuilder.fromUriString(paymentBaseUrl)
                .path("/internal/pay")
                .queryParam("fail", failPayment)
                .queryParam("delayMs", paymentDelayMs)
                .build(true)
                .toUri();
        String body = http.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
        return "checkout-ok:" + body;
    }
}
