package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Test
    void preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
        Optional<Payment> preAuthedPaymentOptional = paymentRepository.findById(savedPayment.getId());
        Payment preAuthedPayment = preAuthedPaymentOptional.get();
        System.out.println(preAuthedPayment);
        System.out.println(sm.getState().getId());
        assertEquals(sm.getState().getId(), PaymentState.PRE_AUTH);
    }

    @Test
    void testAuth() {
            Payment savedPayment = paymentService.newPayment(payment);
            StateMachine<PaymentState, PaymentEvent> preAuthSM = paymentService.preAuth(savedPayment.getId());
            System.out.println(preAuthSM.getState().getId());
            StateMachine<PaymentState, PaymentEvent> authSM = paymentService.authorizePayment(savedPayment.getId());
            Optional<Payment> authedPaymentOptional = paymentRepository.findById(savedPayment.getId());
            Payment authedPayment = authedPaymentOptional.get();
            System.out.println(authedPayment);
            System.out.println(authSM.getState().getId());
            assertEquals(authSM.getState().getId(), PaymentState.AUTH);
        }
}