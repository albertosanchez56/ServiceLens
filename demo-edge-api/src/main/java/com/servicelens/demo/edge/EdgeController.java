package com.servicelens.demo.edge;

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
@RequestMapping("/api")
public class EdgeController {

    private static final Logger log = LoggerFactory.getLogger(EdgeController.class);

    private final RestClient http;
    private final String checkoutBaseUrl;

    public EdgeController(RestClient.Builder restClientBuilder,
                          @Value("${demo.checkout.base-url}") String checkoutBaseUrl) {
        this.http = restClientBuilder.build();
        this.checkoutBaseUrl = checkoutBaseUrl;
    }

    /**
     * Flujo feliz o con fallo simulado en payment (propagado por query params).
     */
    @GetMapping("/flow")
    public String flow(
            @RequestParam(defaultValue = "false") boolean failPayment,
            @RequestParam(defaultValue = "0") long paymentDelayMs) {
        log.info("Edge invoking checkout flow");
        URI uri = UriComponentsBuilder.fromUriString(checkoutBaseUrl)
                .path("/internal/checkout")
                .queryParam("failPayment", failPayment)
                .queryParam("paymentDelayMs", paymentDelayMs)
                .build(true)
                .toUri();
        return http.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
    }
}
