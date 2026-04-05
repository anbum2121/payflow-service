package payflow_service.kafka;

import payflow_service.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private static final String TOPIC = "payment.initiated";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendPaymentEvent(PaymentResponse payment) {
        String message = "PAYMENT_INITIATED::" + payment.getId() + "::" + payment.getAmount() + "::" + payment.getCurrency();
        log.info("Sending payment event to Kafka topic {}: {}", TOPIC, message);
        kafkaTemplate.send(TOPIC, payment.getId().toString(), message);
        log.info("Payment event sent successfully for payment id: {}", payment.getId());
    }
}