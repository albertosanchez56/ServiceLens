package com.servicelens.demo.edge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api")
public class EdgeController {

    private static final Logger log = LoggerFactory.getLogger(EdgeController.class);

    private final RestClient checkoutClient;

    public EdgeController(RestClient.Builder restClientBuilder,
                          @Value("${demo.checkout.base-url}") String checkoutBaseUrl) {
        this.checkoutClient = restClientBuilder.baseUrl(checkoutBaseUrl).build();
    }

    /**
     * Flujo feliz o con fallo simulado en payment (propagado por query params).
     */
    @GetMapping("/flow")
    public String flow(
            @RequestParam(defaultValue = "false") boolean failPayment,
            @RequestParam(defaultValue = "0") long paymentDelayMs) {
        log.info("Edge invoking checkout flow");
        return checkoutClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/checkout")
                        .queryParam("failPayment", failPayment)
                        .queryParam("paymentDelayMs", paymentDelayMs)
                        .build())
                .retrieve()
                .body(String.class);
    }
}
