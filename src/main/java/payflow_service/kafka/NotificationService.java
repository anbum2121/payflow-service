package payflow_service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendPaymentSuccessNotification(String paymentId, String senderId,
                                               String receiverId, String amount) {
        // In production this would send email/SMS/webhook
        log.info("NOTIFICATION: Payment {} successful!", paymentId);
        log.info("Sender: {} -> Receiver: {} | Amount: {}",
                senderId, receiverId, amount);
    }

    public void sendFraudAlertNotification(String senderId, String paymentId) {
        log.warn("FRAUD NOTIFICATION: Payment {} from sender {} flagged as fraudulent!",
                paymentId, senderId);
    }
}