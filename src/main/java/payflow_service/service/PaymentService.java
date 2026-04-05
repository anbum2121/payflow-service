package payflow_service.service;

import payflow_service.dto.PaymentRequest;
import payflow_service.dto.PaymentResponse;
import payflow_service.kafka.PaymentEventProducer;
import payflow_service.model.Payment;
import payflow_service.model.PaymentStatus;
import payflow_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentResponse initiatePayment(PaymentRequest request) {

        // Idempotency check
        Optional<Payment> existing = paymentRepository
                .findByIdempotencyKey(request.getIdempotencyKey());

        if (existing.isPresent()) {
            log.info("Duplicate payment request detected for key: {}",
                    request.getIdempotencyKey());
            return mapToResponse(existing.get());
        }

        // Build and save new payment
        Payment payment = Payment.builder()
                .idempotencyKey(request.getIdempotencyKey())
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.INITIATED)
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment initiated successfully with id: {}", saved.getId());

        // Publish Kafka event
        PaymentResponse response = mapToResponse(saved);
        paymentEventProducer.sendPaymentEvent(response);

        return response;
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .idempotencyKey(payment.getIdempotencyKey())
                .senderId(payment.getSenderId())
                .receiverId(payment.getReceiverId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}