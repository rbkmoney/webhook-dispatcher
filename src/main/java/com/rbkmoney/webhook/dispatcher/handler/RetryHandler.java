package com.rbkmoney.webhook.dispatcher.handler;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.filter.TimeDispatchFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryHandler {

    private final WebHookHandlerImpl handler;
    private final TimeDispatchFilter timeDispatchFilter;

    private static final long MILLIS = 500L;

    public void handle(String topic, Acknowledgment acknowledgment, WebhookMessage webhookMessage, Long timeout) {
        log.info("RetryWebHookHandler webhookMessage: {}", webhookMessage);
        try {
            if (timeDispatchFilter.filter(webhookMessage, timeout)) {
                handler.handle(topic, webhookMessage);
                acknowledgment.acknowledge();
                log.info("Retry webhookMessage: {} is finished", webhookMessage);
            } else {
                Thread.sleep(MILLIS);
                log.info("Waiting timeout: {}", timeout);
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException when listen webhookMessage: {} e: ", webhookMessage, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error when listen webhookMessage: {} e: ", webhookMessage, e);
        }
    }

}
