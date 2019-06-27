package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.Webhook;
import com.rbkmoney.webhook.dispatcher.handler.RetryHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LastRetryWebHookListener {

    @Value("${retry.first.seconds}")
    private long timeout;
    @Value("${kafka.topic.webhook.last.retry}")
    private String postponedTopic;

    private final RetryHandler handler;

    @KafkaListener(topics = "${kafka.topic.webhook.last.retry}", containerFactory = "kafkaLastRetryListenerContainerFactory")
    public void listen(Webhook webhook, Acknowledgment acknowledgment) {
        handler.handle(postponedTopic, acknowledgment, webhook, timeout);
    }

}
