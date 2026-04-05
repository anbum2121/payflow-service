package payflow_service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class FraudDetectionService {

    // In-memory store: senderId -> list of payment timestamps
    private final Map<String, List<LocalDateTime>> paymentTimestamps
            = new ConcurrentHashMap<>();

    private static final int MAX_PAYMENTS_PER_MINUTE = 3;

    public boolean isFraudulent(String senderId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minusMinutes(1);

        // Get or create timestamp list for this sender
        paymentTimestamps.putIfAbsent(senderId, new ArrayList<>());
        List<LocalDateTime> timestamps = paymentTimestamps.get(senderId);

        // Remove timestamps older than 1 minute
        timestamps.removeIf(t -> t.isBefore(oneMinuteAgo));

        // Check if limit exceeded
        if (timestamps.size() >= MAX_PAYMENTS_PER_MINUTE) {
            log.warn("FRAUD ALERT! Sender {} made {} payments in the last 60 seconds",
                    senderId, timestamps.size());
            return true;
        }

        // Record this payment timestamp
        timestamps.add(now);
        return false;
    }
}