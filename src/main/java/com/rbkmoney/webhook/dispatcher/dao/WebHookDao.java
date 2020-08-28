package com.rbkmoney.webhook.dispatcher.dao;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public interface WebHookDao {

    void commit(WebhookMessage webhookMessage);

    void bury(WebhookMessage webhookMessage);

    Boolean isParentCommitted(WebhookMessage webhookMessage);

    Boolean isCommitted(WebhookMessage webhookMessage);

}
