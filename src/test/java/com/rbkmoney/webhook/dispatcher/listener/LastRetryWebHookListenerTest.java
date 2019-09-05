package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.filter.TimeDispatchFilter;
import com.rbkmoney.webhook.dispatcher.handler.WebHookHandlerImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;

public class LastRetryWebHookListenerTest {

    public static final long DEFAULT_TIMEOUT = 1L;
    public static final String TOPIC = "test";
    public static final String SOURCE_ID = "test";
    @Mock
    private WebHookHandlerImpl handler;
    @Mock
    private TimeDispatchFilter timeDispatchFilter;
    @Mock
    private KafkaTemplate<String, WebhookMessage> kafkaTemplate;
    @Mock
    private Acknowledgment acknowledgment;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void listen() {
        LastRetryWebHookListener lastRetryWebHookListener = new LastRetryWebHookListener(DEFAULT_TIMEOUT, TOPIC,
                DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, handler, timeDispatchFilter, kafkaTemplate);
        WebhookMessage webhookMessage = new WebhookMessage()
                .setSourceId(SOURCE_ID);
        Mockito.when(timeDispatchFilter.filter(webhookMessage, 4L)).thenReturn(true);

        lastRetryWebHookListener.onMessage(new ConsumerRecord<>("key", 0,0,"d", webhookMessage), acknowledgment);

        Assert.assertEquals(2L, webhookMessage.getRetryCount());
        Mockito.verify(handler, Mockito.times(1)).handle(TOPIC, webhookMessage);
        Mockito.verify(acknowledgment, Mockito.times(1)).acknowledge();

        Mockito.when(timeDispatchFilter.filter(webhookMessage, 4L)).thenReturn(false);

        lastRetryWebHookListener.onMessage(new ConsumerRecord<>("key", 0,0,"d", webhookMessage), acknowledgment);

        Assert.assertEquals(2L, webhookMessage.getRetryCount());
        Mockito.verify(kafkaTemplate, Mockito.times(1)).send(TOPIC, SOURCE_ID, webhookMessage);

    }

}