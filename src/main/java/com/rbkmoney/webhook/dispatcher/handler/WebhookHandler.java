package com.rbkmoney.webhook.dispatcher.handler;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public interface WebhookHandler {

    void handle(String postponedTopic, WebhookMessage webhookMessage);

}
