package com.rbkmoney.webhook.dispatcher.service;


import com.rbkmoney.webhook.dispatcher.WebhookMessage;

import java.io.IOException;

public interface WebHookDispatcherService {

    int dispatch(WebhookMessage webhookMessage) throws IOException;

}
