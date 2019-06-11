package com.rbkmoney.webhook.dispatcher.serde;

import com.rbkmoney.kafka.common.serialization.AbstractThriftDeserializer;
import com.rbkmoney.webhook.dispatcher.Webhook;

public class WebHookDeserializer extends AbstractThriftDeserializer<Webhook> {

    @Override
    public Webhook deserialize(String s, byte[] bytes) {
        return super.deserialize(bytes, new Webhook());
    }
    
}
