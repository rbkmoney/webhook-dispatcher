package com.rbkmoney.webhook.dispatcher.service;

import com.rbkmoney.webhook.dispatcher.Webhook;
import com.rbkmoney.webhook.dispatcher.dao.WebHookDao;
import com.rbkmoney.webhook.dispatcher.filter.DispatchFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class WebHookHandler {

    private final WebHookDispatcherService webHookDispatcherService;
    private final DispatchFilter postponedDispatchFilter;
    private final WebHookDao webHookDao;
    private final KafkaTemplate<String, Webhook> kafkaTemplate;

    public void handle(String postponedTopic, String key, Webhook webhook) throws IOException {
        if (postponedDispatchFilter.filter(webhook)) {
            kafkaTemplate.send(postponedTopic, key, webhook);
        } else {
            webHookDispatcherService.dispatch(new Webhook());
            webHookDao.commit(webhook);
        }
    }

}
