package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.handler.RetryHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecondRetryWebHookListener extends RetryConsumerSeekAware implements AcknowledgingMessageListener<String, WebhookMessage>, ConsumerSeekAware {

    @Value("${retry.second.seconds}")
    private long timeout;

    @Value("${retry.first.seconds}")
    private long prevTimeout;

    @Value("${kafka.topic.webhook.third.retry}")
    private String postponedTopic;

    private final RetryHandler handler;

    @KafkaListener(topics = "${kafka.topic.webhook.second.retry}", containerFactory = "kafkaSecondRetryListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, WebhookMessage> consumerRecord, Acknowledgment acknowledgment) {
        WebhookMessage webhookMessage = consumerRecord.value();
        log.info("Second retry sourceId: {} webhookId: {} eventId: {}", webhookMessage.getSourceId(),
                webhookMessage.getWebhookId(), webhookMessage.getEventId());
        handler.handle(postponedTopic, acknowledgment, consumerRecord, timeout + prevTimeout, consumerSeekCallback);
    }

}
