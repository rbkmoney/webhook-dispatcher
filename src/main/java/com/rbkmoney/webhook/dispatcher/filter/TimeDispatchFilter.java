package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.Webhook;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TimeDispatchFilter {

    public Boolean filter(Webhook webhook, long timeout) {
        return Instant.now().toEpochMilli() - Instant.parse(webhook.getCreatedAt()).toEpochMilli() > timeout * 1000;
    }

}
