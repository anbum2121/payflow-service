package payflow_service;

import payflow_service.dto.PaymentRequest;
import payflow_service.dto.PaymentResponse;
import payflow_service.kafka.PaymentEventProducer;
import payflow_service.model.Payment;
import payflow_service.model.PaymentStatus;
import payflow_service.repository.PaymentRepository;
import payflow_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest buildRequest(String idempotencyKey) {
        PaymentRequest request = new PaymentRequest();
        request.setSenderId("user_001");
        request.setReceiverId("user_002");
        request.setAmount(new BigDecimal("500.00"));
        request.setCurrency("INR");
        request.setIdempotencyKey(idempotencyKey);
        return request;
    }

    private Payment buildPayment(String idempotencyKey) {
        return Payment.builder()
                .id(1L)
                .senderId("user_001")
                .receiverId("user_002")
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .idempotencyKey(idempotencyKey)
                .status(PaymentStatus.INITIATED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreatePaymentSuccessfully() {
        PaymentRequest request = buildRequest("txn_001");
        Payment savedPayment = buildPayment("txn_001");

        when(paymentRepository.findByIdempotencyKey("txn_001"))
                .thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        PaymentResponse response = paymentService.initiatePayment(request);

        assertNotNull(response);
        assertEquals("txn_001", response.getIdempotencyKey());
        assertEquals(PaymentStatus.INITIATED, response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentEventProducer, times(1)).sendPaymentEvent(any());
    }

    @Test
    void shouldReturnExistingPaymentForDuplicateRequest() {
        PaymentRequest request = buildRequest("txn_001");
        Payment existingPayment = buildPayment("txn_001");

        when(paymentRepository.findByIdempotencyKey("txn_001"))
                .thenReturn(Optional.of(existingPayment));

        PaymentResponse response = paymentService.initiatePayment(request);

        assertNotNull(response);
        assertEquals("txn_001", response.getIdempotencyKey());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentEventProducer, never()).sendPaymentEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenPaymentNotFound() {
        when(paymentRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.getPaymentById(999L));

        assertEquals("Payment not found with id: 999", exception.getMessage());
    }
}