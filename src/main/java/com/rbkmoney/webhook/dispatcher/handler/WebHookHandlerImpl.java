package com.rbkmoney.webhook.dispatcher.handler;

import com.rbkmoney.kafka.common.exception.RetryableException;
import com.rbkmoney.webhook.dispatcher.Webhook;
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
    private final WebHookDao webHookDao;
    private final KafkaTemplate<String, Webhook> kafkaTemplate;

    @Value("${kafka.topic.webhook.dead.letter.queue}")
    private String dlq;

    @Override
    public void handle(String postponedTopic, Webhook webhook) {
        try {
            if (!deadRetryDispatchFilter.filter(webhook)) {
                log.debug("Retry time is end for webhook: {}", webhook);
                kafkaTemplate.send(dlq, webhook.source_id, webhook);
            } else if (postponedDispatchFilter.filter(webhook)) {
                log.debug("Resend to topic: {} webhook: {}", postponedTopic, webhook);
                kafkaTemplate.send(postponedTopic, webhook.source_id, webhook);
            } else {
                webHookDispatcherService.dispatch(webhook);
                webHookDao.commit(webhook);
            }
        } catch (RetryableException e) {
            log.warn("Error when handle webhook: {}", webhook, e);
            kafkaTemplate.send(postponedTopic, webhook.source_id, webhook);
        } catch (Exception e) {
            log.error("Send to dlq webhook: {} by e:", webhook, e);
            kafkaTemplate.send(dlq, webhook.source_id, webhook);
        }
    }

}
