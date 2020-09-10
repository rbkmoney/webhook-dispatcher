package com.rbkmoney.webhook.dispatcher.service;


import com.rbkmoney.webhook.dispatcher.WebhookMessage;

import java.io.IOException;

public interface WebhookDispatcherService {

    int dispatch(WebhookMessage webhookMessage) throws IOException;

}
