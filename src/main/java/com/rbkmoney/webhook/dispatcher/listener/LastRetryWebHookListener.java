package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.handler.RetryHandler;
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
public class LastRetryWebHookListener extends RetryConsumerSeekAware implements AcknowledgingMessageListener<String, WebhookMessage>, ConsumerSeekAware {

    public static final int COUNT_FIRST_RETRIES = 3;

    private final long timeout;
    private final String postponedTopic;
    private final long thirdTimeout;
    private final long firstTimeout;
    private final long secondTimeout;

    private final RetryHandler handler;

    public LastRetryWebHookListener(@Value("${retry.last.seconds}") long timeout,
                                    @Value("${kafka.topic.webhook.last.retry}") String postponedTopic,
                                    @Value("${retry.third.seconds}") long thirdTimeout,
                                    @Value("${retry.first.seconds}") long firstTimeout,
                                    @Value("${retry.second.seconds}") long secondTimeout,
                                    RetryHandler handler) {
        this.timeout = timeout;
        this.postponedTopic = postponedTopic;
        this.thirdTimeout = thirdTimeout;
        this.firstTimeout = firstTimeout;
        this.secondTimeout = secondTimeout;
        this.handler = handler;
    }

    @KafkaListener(topics = "${kafka.topic.webhook.last.retry}", containerFactory = "kafkaLastRetryListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, WebhookMessage> consumerRecord, Acknowledgment acknowledgment) {
        long retryCount = initRetryCount(consumerRecord.value());
        handler.handle(postponedTopic, acknowledgment, consumerRecord, initTimeout(retryCount), consumerSeekCallback);
    }

    private long initTimeout(long retryCount) {
        return (timeout * retryCount) + firstTimeout + secondTimeout + thirdTimeout;
    }

    private long initRetryCount(WebhookMessage webhookMessage) {
        long retryCount = webhookMessage.getRetryCount();
        if (retryCount > 2) {
            return retryCount - COUNT_FIRST_RETRIES;
        }
        return 1;
    }

}
