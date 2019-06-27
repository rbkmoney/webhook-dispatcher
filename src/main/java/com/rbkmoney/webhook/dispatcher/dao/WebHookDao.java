package com.rbkmoney.webhook.dispatcher.dao;

import com.rbkmoney.webhook.dispatcher.Webhook;

public interface WebHookDao {

    void commit(Webhook webHook);

    boolean isParentCommitted(Webhook webHook);

}
