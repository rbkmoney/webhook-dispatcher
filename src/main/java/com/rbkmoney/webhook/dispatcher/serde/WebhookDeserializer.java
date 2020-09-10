package com.rbkmoney.webhook.dispatcher.serde;

import com.rbkmoney.kafka.common.serialization.AbstractThriftDeserializer;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public class WebhookDeserializer extends AbstractThriftDeserializer<WebhookMessage> {

    @Override
    public WebhookMessage deserialize(String s, byte[] bytes) {
        return super.deserialize(bytes, new WebhookMessage());
    }
    
}
