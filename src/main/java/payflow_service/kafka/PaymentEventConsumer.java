package payflow_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final FraudDetectionService fraudDetectionService;
    private final NotificationService notificationService;

    @KafkaListener(topics = "payment.initiated", groupId = "payflow-group")
    public void consumePaymentEvent(String message) {
        log.info("Received payment event from Kafka: {}", message);

        String[] parts = message.split("::");
        if (parts.length == 4) {
            String paymentId = parts[1];
            String amount = parts[2];
            String currency = parts[3];
            String senderId = parts[0].replace("PAYMENT_INITIATED", "").trim();

            log.info("Processing payment - ID: {}, Amount: {} {}",
                    paymentId, amount, currency);

            // Fraud check
            boolean fraudulent = fraudDetectionService.isFraudulent(senderId);

            if (fraudulent) {
                notificationService.sendFraudAlertNotification(senderId, paymentId);
                log.warn("Payment {} blocked due to fraud detection!", paymentId);
            } else {
                notificationService.sendPaymentSuccessNotification(
                        paymentId, senderId, "receiver", amount);
                log.info("Payment {} processed successfully!", paymentId);
            }
        }
    }
}