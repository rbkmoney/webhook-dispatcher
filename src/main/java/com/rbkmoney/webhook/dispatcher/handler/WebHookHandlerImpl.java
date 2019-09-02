package com.rbkmoney.webhook.dispatcher.handler;

import com.rbkmoney.kafka.common.exception.RetryableException;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.dao.WebHookDao;
import com.rbkmoney.webhook.dispatcher.filter.DispatchFilter;
import com.rbkmoney.webhook.dispatcher.service.WebHookDispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebHookHandlerImpl implements WebHookHandler {

    private final WebHookDispatcherService webHookDispatcherService;
    private final DispatchFilter postponedDispatchFilter;
    private final DispatchFilter deadRetryDispatchFilter;
    private final WebHookDao webHookDaoPgImpl;
    private final KafkaTemplate<String, WebhookMessage> kafkaTemplate;

    @Value("${kafka.topic.webhook.dead.letter.queue}")
    private String dlq;

    @Override
    public void handle(String postponedTopic, WebhookMessage webhookMessage) {
        try {
            if (deadRetryDispatchFilter.filter(webhookMessage)) {
                log.warn("Retry time is end for webhookMessage: {}", webhookMessage);
                kafkaTemplate.send(dlq, webhookMessage.source_id, webhookMessage);
            } else if (postponedDispatchFilter.filter(webhookMessage)) {
                log.info("Resend to topic: {} webhookMessage: {}", postponedTopic, webhookMessage);
                kafkaTemplate.send(postponedTopic, webhookMessage.source_id, webhookMessage);
            } else {
                webHookDispatcherService.dispatch(webhookMessage);
                webHookDaoPgImpl.commit(webhookMessage);
            }
        } catch (RetryableException e) {
            log.warn("Error when handle webhookMessage: {}", webhookMessage, e);
            kafkaTemplate.send(postponedTopic, webhookMessage.source_id, webhookMessage);
            log.info("Send to retry topic: {} source_id: {} message: {}", postponedTopic, webhookMessage.source_id, webhookMessage);
        } catch (Exception e) {
            log.error("Send to dlq webhookMessage: {} by e:", webhookMessage, e);
            kafkaTemplate.send(dlq, webhookMessage.source_id, webhookMessage);
            log.info("Send to dlq topic: {} source_id: {} message: {}", dlq, webhookMessage.source_id, webhookMessage);
        }
    }

}
