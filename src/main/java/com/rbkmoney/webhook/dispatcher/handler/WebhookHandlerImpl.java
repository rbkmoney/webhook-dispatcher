package com.rbkmoney.webhook.dispatcher.handler;

import com.rbkmoney.kafka.common.exception.RetryableException;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.dao.WebhookDao;
import com.rbkmoney.webhook.dispatcher.filter.DispatchFilter;
import com.rbkmoney.webhook.dispatcher.service.WebhookDispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.rbkmoney.webhook.dispatcher.utils.WebhookLogUtils.info;
import static com.rbkmoney.webhook.dispatcher.utils.WebhookLogUtils.warn;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookHandlerImpl implements WebhookHandler {

    private final WebhookDispatcherService webhookDispatcherService;
    private final DispatchFilter postponedDispatchFilter;
    private final DispatchFilter deadRetryDispatchFilter;
    private final WebhookDao webhookDao;
    private final KafkaTemplate<String, WebhookMessage> kafkaTemplate;

    @Override
    public void handle(String postponedTopic, WebhookMessage webhookMessage) {
        try {
            if (deadRetryDispatchFilter.filter(webhookMessage)) {
                warn("Retry time has ended for", webhookMessage);
                webhookDao.bury(webhookMessage);
            } else if (postponedDispatchFilter.filter(webhookMessage)) {
                long retryCount = webhookMessage.getRetryCount();
                webhookMessage.setRetryCount(++retryCount);
                info("Resend to topic: " + postponedTopic, webhookMessage);
                kafkaTemplate.send(postponedTopic, webhookMessage.source_id, webhookMessage).get();
            } else {
                long retryCount = webhookMessage.getRetryCount();
                webhookMessage.setRetryCount(++retryCount);
                info("Dispatch", webhookMessage);
                webhookDispatcherService.dispatch(webhookMessage);
                webhookDao.commit(webhookMessage);
            }
        } catch (RetryableException e) {
            log.warn("RetryableException during webhook handling", e);
            syncSendMessage(postponedTopic, webhookMessage);
            info("Send to retry topic: " + postponedTopic, webhookMessage);
        } catch (Exception e) {
            log.error("Exception during webhook handling", e);
            throw new RuntimeException("Exception during webhook handling", e);
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
