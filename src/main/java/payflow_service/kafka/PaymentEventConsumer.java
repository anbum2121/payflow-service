package payflow_service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentEventConsumer {

    @KafkaListener(topics = "payment.initiated", groupId = "payflow-group")
    public void consumePaymentEvent(String message) {
        log.info("Received payment event from Kafka: {}", message);

        String[] parts = message.split("::");
        if (parts.length == 4) {
            String paymentId = parts[1];
            String amount = parts[2];
            String currency = parts[3];
            log.info("Processing payment - ID: {}, Amount: {} {}",
                    paymentId, amount, currency);
            // Ledger logic will go here in next phase
        }
    }
}