package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.util.Optional;

@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

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
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = buildStateMachine(paymentId);
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = buildStateMachine(paymentId);
        return null;
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
