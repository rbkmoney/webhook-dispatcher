package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.Webhook;
import com.rbkmoney.webhook.dispatcher.utils.TimeoutUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TimeDispatchFilter {

    public Boolean filter(Webhook webhook, long timeout) {
        return TimeoutUtils.calculateTimeFromCreated(webhook.getCreatedAt()) > TimeUnit.MILLISECONDS.toSeconds(timeout);
    }

}
