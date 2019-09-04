package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.filter.TimeDispatchFilter;
import com.rbkmoney.webhook.dispatcher.handler.WebHookHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LastRetryWebHookListener {

    public static final long WAITING_PERIOD = 500L;

    private long timeout;
    private String postponedTopic;
    private long thirdTimeout;
    private long firstTimeout;
    private long secondTimeout;

    private final WebHookHandlerImpl handler;
    private final TimeDispatchFilter timeDispatchFilter;
    private final KafkaTemplate<String, WebhookMessage> kafkaTemplate;

    public LastRetryWebHookListener(@Value("${retry.last.seconds}") long timeout,
                                    @Value("${kafka.topic.webhook.last.retry}") String postponedTopic,
                                    @Value("${retry.third.seconds}") long thirdTimeout,
                                    @Value("${retry.first.seconds}") long firstTimeout,
                                    @Value("${retry.second.seconds}") long secondTimeout,
                                    WebHookHandlerImpl handler,
                                    TimeDispatchFilter timeDispatchFilter,
                                    KafkaTemplate<String, WebhookMessage> kafkaTemplate) {
        this.timeout = timeout;
        this.postponedTopic = postponedTopic;
        this.thirdTimeout = thirdTimeout;
        this.firstTimeout = firstTimeout;
        this.secondTimeout = secondTimeout;
        this.handler = handler;
        this.timeDispatchFilter = timeDispatchFilter;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${kafka.topic.webhook.last.retry}", containerFactory = "kafkaLastRetryListenerContainerFactory")
    public void listen(WebhookMessage webhookMessage, Acknowledgment acknowledgment) {
        log.info("LastRetryWebHookListener webhookMessage: {}", webhookMessage);
        try {
            long retryCount = initRetryCount(webhookMessage);
            long timeout = initTimeout(retryCount);
            if (timeDispatchFilter.filter(webhookMessage, timeout)) {
                webhookMessage.setRetryCount(++retryCount);
                handler.handle(postponedTopic, webhookMessage);
                acknowledgment.acknowledge();
                log.info("Retry webhookMessage: {} is finished", webhookMessage);
            } else {
                log.warn("Waiting when handle webhookMessage: {}", webhookMessage);
                kafkaTemplate.send(postponedTopic, webhookMessage.source_id, webhookMessage);
                acknowledgment.acknowledge();
                log.info("ReSend to retry without count++ topic: {} source_id: {} message: {}", postponedTopic, webhookMessage.source_id, webhookMessage);
                Thread.sleep(WAITING_PERIOD);
                log.info("Waiting timeout: {}", timeout);
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException when listen webhookMessage: {} ", webhookMessage, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error when listen webhookMessage: {} ", webhookMessage, e);
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
