package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public interface DispatchFilter {

    Boolean filter(WebhookMessage t);

}
