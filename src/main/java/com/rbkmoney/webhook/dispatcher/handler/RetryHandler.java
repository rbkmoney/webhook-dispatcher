package com.rbkmoney.webhook.dispatcher.handler;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.filter.DispatchFilter;
import com.rbkmoney.webhook.dispatcher.filter.TimeDispatchFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryHandler {

    private final WebHookHandlerImpl handler;
    private final TimeDispatchFilter timeDispatchFilter;

    private static final long WAITING_PERIOD = 500L;

    public void handle(String topic, Acknowledgment acknowledgment, ConsumerRecord<String, WebhookMessage> consumerRecord,
                       Long timeout, ThreadLocal<ConsumerSeekAware.ConsumerSeekCallback> consumerSeekCallback) {
        WebhookMessage webhookMessage = consumerRecord.value();
        if (timeDispatchFilter.filter(webhookMessage, timeout)) {
            handler.handle(topic, webhookMessage);
            acknowledgment.acknowledge();
        } else {
            try {
                consumerSeekCallback.get().seek(consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());
                safeSleep();
                log.debug("Waiting timeout: {}", timeout);
            } catch (Exception e) {
                log.warn("Problem with seek aware e: ", e);
            }
        }
    }

    private void safeSleep() {
        try {
            Thread.sleep(WAITING_PERIOD);
        } catch (InterruptedException e) {
            log.warn("Interrupted exception when sleep!", e);
            Thread.currentThread().interrupt();
        }
    }

}
