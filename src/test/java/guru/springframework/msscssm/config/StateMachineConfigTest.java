package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    private StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    @Test
    void testNewStateMachine() {
        StateMachine<PaymentState, PaymentEvent> stateMachine = stateMachineFactory.getStateMachine(UUID.randomUUID());
        stateMachine.start();

        System.out.println(stateMachine.getState().toString());
        assertEquals(stateMachine.getState().getId(), PaymentState.NEW);

        stateMachine.sendEvent(PaymentEvent.PRE_AUTHORIZE);
        System.out.println(stateMachine.getState().toString());

        assertEquals(stateMachine.getState().getId(), PaymentState.NEW);

        stateMachine.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);
        System.out.println(stateMachine.getState().toString());

        assertEquals(stateMachine.getState().getId(), PaymentState.PRE_AUTH);

        stateMachine.sendEvent(PaymentEvent.PRE_AUTH_DECLINED);
        System.out.println(stateMachine.getState().toString());

        assertEquals(stateMachine.getState().getId(), PaymentState.PRE_AUTH);
    }
}