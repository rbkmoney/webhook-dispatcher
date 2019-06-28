package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.handler.WebHookHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebHookListener {

    private final WebHookHandler handler;

    @Value("${kafka.topic.webhook.first.retry}")
    private String postponedTopic;

    @KafkaListener(topics = "${kafka.topic.webhook.forward}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(WebhookMessage webhookMessage, Acknowledgment acknowledgment) {
        log.info("WebHookListener webhook: {}", webhookMessage);
        try {
            handler.handle(postponedTopic, webhookMessage);
            acknowledgment.acknowledge();
        }  catch (Exception e) {
            log.error("Error when listen webhook value: {} e: ", webhookMessage, e);
        }
    }

}
