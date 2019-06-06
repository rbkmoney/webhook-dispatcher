package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.Webhook;
import org.springframework.stereotype.Component;

@Component
public class TimeDispatchFilter implements DispatchFilter {
    @Override
    public Boolean filter(Webhook t) {
        return null;
    }
}
