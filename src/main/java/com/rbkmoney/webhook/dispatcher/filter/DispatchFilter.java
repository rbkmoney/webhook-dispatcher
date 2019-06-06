package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.Webhook;

public interface DispatchFilter {

    Boolean filter(Webhook t);

}
