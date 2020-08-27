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

import static com.rbkmoney.webhook.dispatcher.utils.WebHookLogUtils.info;
import static com.rbkmoney.webhook.dispatcher.utils.WebHookLogUtils.warn;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebHookHandlerImpl implements WebHookHandler {

    private final WebHookDispatcherService webHookDispatcherService;
    private final DispatchFilter postponedDispatchFilter;
    private final DispatchFilter deadRetryDispatchFilter;
    private final WebHookDao webHookDao;
    private final KafkaTemplate<String, WebhookMessage> kafkaTemplate;

    @Value("${kafka.topic.webhook.dead.letter.queue}")
    private String dlq;

    @Override
    public void handle(String postponedTopic, WebhookMessage webhookMessage) {
        try {
            if (deadRetryDispatchFilter.filter(webhookMessage)) {
                warn("Retry time is end for", webhookMessage);
                kafkaTemplate.send(dlq, webhookMessage.source_id, webhookMessage).get();
            } else if (postponedDispatchFilter.filter(webhookMessage)) {
                long retryCount = webhookMessage.getRetryCount();
                webhookMessage.setRetryCount(++retryCount);
                info("Resend to topic: " + postponedTopic, webhookMessage);
                kafkaTemplate.send(postponedTopic, webhookMessage.source_id, webhookMessage).get();
            } else {
                long retryCount = webhookMessage.getRetryCount();
                webhookMessage.setRetryCount(++retryCount);
                info("Dispatch", webhookMessage);
                webHookDispatcherService.dispatch(webhookMessage);
                webHookDao.commit(webhookMessage);
            }
        } catch (RetryableException e) {
            log.warn("RetryableException when handle e: ", e);
            syncSendMessage(postponedTopic, webhookMessage);
            info("Send to retry topic: " + postponedTopic, webhookMessage);
        } catch (Exception e) {
            log.error("Exception when handle e:", e);
            throw new RuntimeException("Exception when handle!", e);
        }
    }

    private void syncSendMessage(String postponedTopic, WebhookMessage webhookMessage) {
        try {
            kafkaTemplate.send(postponedTopic, webhookMessage.source_id, webhookMessage).get();
        } catch (Exception e) {
            throw new RuntimeException("Problem with kafkaTemplate send message!", e);
        }
    }

}
