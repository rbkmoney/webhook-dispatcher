package com.rbkmoney.webhook.dispatcher.handler;

import com.rbkmoney.webhook.dispatcher.Webhook;

public interface WebHookHandler {

    void handle(String postponedTopic, Webhook webhook);

}
