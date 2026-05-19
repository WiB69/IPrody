package com.iprody.crm.paymentservice.statemachine;

import com.iprody.crm.paymentservice.exception.PaymentException;
import com.iprody.crm.paymentservice.model.enums.PaymentState;
import com.iprody.crm.paymentservice.model.enums.PaymentStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PaymentStateMachine {

    private final Map<PaymentState, Map<PaymentStateEvent, PaymentState>> transitions = new HashMap<>();

    public PaymentStateMachine() {
        initializeTransitions();
    }

    private void initializeTransitions() {
        Map<PaymentStateEvent, PaymentState> fromNullMap = new EnumMap<>(PaymentStateEvent.class);
        fromNullMap.put(PaymentStateEvent.CREATE, PaymentState.CREATED);
        transitions.put(null, fromNullMap);

        // From CREATED
        Map<PaymentStateEvent, PaymentState> fromCreated = new EnumMap<>(PaymentStateEvent.class);
        fromCreated.put(PaymentStateEvent.PROCESS, PaymentState.PROCESSING);
        fromCreated.put(PaymentStateEvent.FAIL, PaymentState.FAILED);
        fromCreated.put(PaymentStateEvent.SEND_TO_DLQ, PaymentState.IN_DLQ);
        transitions.put(PaymentState.CREATED, fromCreated);

        // From PROCESSING
        Map<PaymentStateEvent, PaymentState> fromProcessing = new EnumMap<>(PaymentStateEvent.class);
        fromProcessing.put(PaymentStateEvent.SUCCEED, PaymentState.SUCCEEDED);
        fromProcessing.put(PaymentStateEvent.FAIL, PaymentState.FAILED);
        fromProcessing.put(PaymentStateEvent.SEND_TO_DLQ, PaymentState.IN_DLQ);
        transitions.put(PaymentState.PROCESSING, fromProcessing);

        // Terminated states
        transitions.put(PaymentState.SUCCEEDED, new EnumMap<>(PaymentStateEvent.class));
        transitions.put(PaymentState.FAILED, new EnumMap<>(PaymentStateEvent.class));
        transitions.put(PaymentState.IN_DLQ, new EnumMap<>(PaymentStateEvent.class));
    }

    public PaymentState transition(PaymentState currentState, PaymentStateEvent event) {
        Map<PaymentStateEvent, PaymentState> stateTransitions = transitions.get(currentState);

        if (stateTransitions == null || !stateTransitions.containsKey(event)) {
            log.error("Invalid transition from state {} with event {}", currentState, event);
            throw new PaymentException(String.format("Invalid transition from state %s with event %s",
                    currentState, event));
        }

        PaymentState newState = stateTransitions.get(event);
        log.debug("State transition: {} --{}--> {}", currentState, event, newState);
        return newState;
    }
}