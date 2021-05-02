package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.util.Optional;

@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository paymentRepository;

    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setPaymentState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = buildStateMachine(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = buildStateMachine(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTH_APPROVED);
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = buildStateMachine(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);
        return null;
    }

    private void sendEvent(long paymentId, StateMachine<PaymentState, PaymentEvent> sm,
                                                               PaymentEvent paymentEvent) {
        Message msg = MessageBuilder.withPayload(paymentEvent)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();
        sm.sendEvent(msg);
    }

    private StateMachine<PaymentState, PaymentEvent> buildStateMachine(long paymentId) {
        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
        Payment payment = paymentOptional.orElseThrow(() -> new RuntimeException("Could not find Payment by id."));
        //Builds state machine with given id!
        StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(String.valueOf(payment.getId()));
        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.resetStateMachine(new DefaultStateMachineContext<PaymentState, PaymentEvent>(
                            payment.getPaymentState(), null, null, null));
                });
        sm.start();
        return sm;
    }
}
