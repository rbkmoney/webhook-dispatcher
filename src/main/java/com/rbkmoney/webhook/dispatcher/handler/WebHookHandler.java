package com.rbkmoney.webhook.dispatcher.handler;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public interface WebHookHandler {

    void handle(String postponedTopic, WebhookMessage webhookMessage);

}
