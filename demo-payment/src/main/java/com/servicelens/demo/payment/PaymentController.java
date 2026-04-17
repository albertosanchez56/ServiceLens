package com.servicelens.demo.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @GetMapping("/pay")
    public String pay(
            @RequestParam(defaultValue = "false") boolean fail,
            @RequestParam(defaultValue = "0") long delayMs) {
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted", e);
            }
        }
        if (fail) {
            log.warn("Simulated payment failure");
            throw new IllegalStateException("Simulated payment failure");
        }
        log.info("Payment OK");
        return "paid";
    }
}
