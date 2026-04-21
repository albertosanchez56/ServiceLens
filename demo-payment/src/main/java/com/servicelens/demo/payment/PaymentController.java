package com.servicelens.demo.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@RestController
@RequestMapping("/internal")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final Counter simulatedFailures;

    public PaymentController(MeterRegistry registry) {
        this.simulatedFailures = Counter.builder("servicelens.demo.payment.simulated_failures")
                .description("Fallos simulados para demo de deteccion ServiceLens")
                .register(registry);
    }

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
            simulatedFailures.increment();
            log.warn("Simulated payment failure");
            throw new IllegalStateException("Simulated payment failure");
        }
        log.info("Payment OK");
        return "paid";
    }
}
