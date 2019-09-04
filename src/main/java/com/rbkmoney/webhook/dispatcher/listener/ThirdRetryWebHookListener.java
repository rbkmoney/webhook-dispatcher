package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
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
public class ThirdRetryWebHookListener {

    @Value("${retry.third.seconds}")
    private long timeout;
    @Value("${retry.first.seconds}")
    private long firstTimeout;
    @Value("${retry.second.seconds}")
    private long secondTimeout;

    @Value("${kafka.topic.webhook.last.retry}")
    private String postponedTopic;
    private final RetryHandler handler;

    @KafkaListener(topics = "${kafka.topic.webhook.third.retry}", containerFactory = "kafkaThirdRetryListenerContainerFactory")
    public void listen(WebhookMessage webhookMessage, Acknowledgment acknowledgment) {
        log.info("Third retry sourceId: {} webhookId: {} eventId: {}", webhookMessage.getSourceId(),
                webhookMessage.getWebhookId(), webhookMessage.getEventId());
        handler.handle(postponedTopic, acknowledgment, webhookMessage, timeout + firstTimeout + secondTimeout);
    }

}
