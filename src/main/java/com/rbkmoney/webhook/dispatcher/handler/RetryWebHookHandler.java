package com.rbkmoney.webhook.dispatcher.handler;

import com.rbkmoney.webhook.dispatcher.Webhook;
import com.rbkmoney.webhook.dispatcher.filter.TimeDispatchFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryWebHookHandler {

    private final WebHookHandlerImpl handler;
    private final TimeDispatchFilter timeDispatchFilter;

    private static final long MILLIS = 500L;

    public void handle(String topic, Acknowledgment acknowledgment, Webhook webhook, Long timeout) {
        log.info("RetryWebHookHandler webhook: {}", webhook);
        try {
            if (timeDispatchFilter.filter(webhook, timeout)) {
                handler.handle(topic, webhook);
                acknowledgment.acknowledge();
            }
            Thread.sleep(MILLIS);
        } catch (Exception e) {
            log.error("Error when listen webhook key: {} value: {} e: ", webhook, e);
        }
    }

}
