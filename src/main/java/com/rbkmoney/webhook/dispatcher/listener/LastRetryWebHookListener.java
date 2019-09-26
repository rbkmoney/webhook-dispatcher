package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.filter.DispatchFilter;
import com.rbkmoney.webhook.dispatcher.filter.TimeDispatchFilter;
import com.rbkmoney.webhook.dispatcher.handler.WebHookHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LastRetryWebHookListener extends RetryConsumerSeekAware implements AcknowledgingMessageListener<String, WebhookMessage>, ConsumerSeekAware {

    public static final long WAITING_PERIOD = 500L;

    private long timeout;
    private String postponedTopic;
    private String dlq;
    private long thirdTimeout;
    private long firstTimeout;
    private long secondTimeout;

    private final WebHookHandlerImpl handler;
    private final TimeDispatchFilter timeDispatchFilter;
    private final KafkaTemplate<String, WebhookMessage> kafkaTemplate;
    private final DispatchFilter deadRetryDispatchFilter;

    public LastRetryWebHookListener(@Value("${retry.last.seconds}") long timeout,
                                    @Value("${kafka.topic.webhook.last.retry}") String postponedTopic,
                                    @Value("${kafka.topic.webhook.dead.letter.queue}") String dlq,
                                    @Value("${retry.third.seconds}") long thirdTimeout,
                                    @Value("${retry.first.seconds}") long firstTimeout,
                                    @Value("${retry.second.seconds}") long secondTimeout,
                                    WebHookHandlerImpl handler,
                                    TimeDispatchFilter timeDispatchFilter,
                                    KafkaTemplate<String, WebhookMessage> kafkaTemplate,
                                    DispatchFilter deadRetryDispatchFilter) {
        this.timeout = timeout;
        this.postponedTopic = postponedTopic;
        this.dlq = dlq;
        this.thirdTimeout = thirdTimeout;
        this.firstTimeout = firstTimeout;
        this.secondTimeout = secondTimeout;
        this.handler = handler;
        this.timeDispatchFilter = timeDispatchFilter;
        this.kafkaTemplate = kafkaTemplate;
        this.deadRetryDispatchFilter = deadRetryDispatchFilter;
    }

    @KafkaListener(topics = "${kafka.topic.webhook.last.retry}", containerFactory = "kafkaLastRetryListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, WebhookMessage> consumerRecord, Acknowledgment acknowledgment) {
        WebhookMessage webhookMessage = consumerRecord.value();
        log.info("LastRetryWebHookListener webhookMessage: {}", webhookMessage);
        long retryCount = initRetryCount(webhookMessage);
        long timeout = initTimeout(retryCount);
        if (deadRetryDispatchFilter.filter(webhookMessage)) {
            log.warn("Retry time is end for webhookMessage: {}", webhookMessage);
            kafkaTemplate.send(dlq, webhookMessage.source_id, webhookMessage);
            acknowledgment.acknowledge();
        } else if (timeDispatchFilter.filter(webhookMessage, timeout)) {
            webhookMessage.setRetryCount(++retryCount);
            handler.handle(postponedTopic, webhookMessage);
            acknowledgment.acknowledge();
            log.info("Retry webhookMessage: {} is finished", webhookMessage);
        } else {
            log.warn("Waiting when handle webhookMessage: {}", webhookMessage);
            kafkaTemplate.send(postponedTopic, webhookMessage.source_id, webhookMessage);
            acknowledgment.acknowledge();
            log.info("ReSend to retry without count++ topic: {} source_id: {} message: {}", postponedTopic, webhookMessage.source_id, webhookMessage);
            safeSleep();
            log.info("Waiting timeout: {}", timeout);
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

    private long initTimeout(long retryCount) {
        return (timeout * retryCount) + firstTimeout + secondTimeout + thirdTimeout;
    }

    private long initRetryCount(WebhookMessage webhookMessage) {
        long retryCount = webhookMessage.getRetryCount();
        if (retryCount == 0) {
            retryCount = 1;
        }
        return retryCount;
    }

}
