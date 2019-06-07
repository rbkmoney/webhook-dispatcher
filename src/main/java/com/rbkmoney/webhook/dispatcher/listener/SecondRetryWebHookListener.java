package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.Webhook;
import com.rbkmoney.webhook.dispatcher.filter.TimeDispatchFilter;
import com.rbkmoney.webhook.dispatcher.service.WebHookHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecondRetryWebHookListener {

    private final TimeDispatchFilter timeDispatchFilter;
    private final WebHookHandler handler;

    @Value("${kafka.topic.webhook.dead.letter.queue}")
    private String postponedTopic;

    private static final long MILLIS = 1000L;

    @KafkaListener(topics = "${kafka.topic.webhook.second.retry}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(String key, Webhook webhook, Acknowledgment acknowledgment) {
        log.info("WebHookListener webhook: {}", webhook);
        try {
            if (timeDispatchFilter.filter(webhook, 600L)) {
                handler.handle(postponedTopic, key, webhook);
                acknowledgment.acknowledge();
            }
            Thread.sleep(MILLIS);
        } catch (Exception e) {
            log.error("Erro when listen webhook key: {} value: {} e: ", key, webhook, e);
        }
    }

}
