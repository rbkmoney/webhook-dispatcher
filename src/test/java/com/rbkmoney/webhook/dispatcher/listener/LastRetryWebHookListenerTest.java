package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.dao.WebHookDao;
import com.rbkmoney.webhook.dispatcher.filter.DeadRetryDispatchFilter;
import com.rbkmoney.webhook.dispatcher.filter.PostponedDispatchFilter;
import com.rbkmoney.webhook.dispatcher.filter.TimeDispatchFilter;
import com.rbkmoney.webhook.dispatcher.handler.RetryHandler;
import com.rbkmoney.webhook.dispatcher.handler.WebHookHandlerImpl;
import com.rbkmoney.webhook.dispatcher.service.WebHookDispatcherService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.Acknowledgment;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;

public class LastRetryWebHookListenerTest {

    public static final long DEFAULT_TIMEOUT = 1L;
    public static final String TOPIC = "test";
    public static final String DLQ_TOPIC = "dlqTest";
    public static final String SOURCE_ID = "test";

    private RetryHandler handler;

    @Mock
    private TimeDispatchFilter timeDispatchFilter;

    @Mock
    private WebHookDispatcherService webHookDispatcherService;
    @Mock
    private DeadRetryDispatchFilter deadRetryDispatchFilter;
    @Mock
    private PostponedDispatchFilter postponedDispatchFilter;
    @Mock
    private KafkaTemplate<String, WebhookMessage> kafkaTemplate;
    @Mock
    private Acknowledgment acknowledgment;
    @Mock
    private WebHookDao webHookDaoPgImpl;
    @Mock
    private ConsumerSeekAware.ConsumerSeekCallback consumerSeekCallback;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        WebHookHandlerImpl handler = new WebHookHandlerImpl(webHookDispatcherService, postponedDispatchFilter, deadRetryDispatchFilter, webHookDaoPgImpl, kafkaTemplate);
        this.handler = new RetryHandler(handler, timeDispatchFilter, deadRetryDispatchFilter, kafkaTemplate);
    }

    @Test
    public void listen() throws IOException {
        LastRetryWebHookListener lastRetryWebHookListener = new LastRetryWebHookListener(DEFAULT_TIMEOUT, TOPIC,
                DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, handler);
        lastRetryWebHookListener.registerSeekCallback(consumerSeekCallback);

        WebhookMessage webhookMessage = new WebhookMessage()
                .setSourceId(SOURCE_ID);
        Mockito.when(timeDispatchFilter.filter(webhookMessage, 4L)).thenReturn(true);
        Mockito.when(deadRetryDispatchFilter.filter(webhookMessage)).thenReturn(false);
        Mockito.when(postponedDispatchFilter.filter(webhookMessage)).thenReturn(false);
        Mockito.doNothing().when(webHookDaoPgImpl).commit(webhookMessage);

        ConsumerRecord<String, WebhookMessage> consumerRecord = new ConsumerRecord<>("key", 0, 0, "d", webhookMessage);
        lastRetryWebHookListener.onMessage(consumerRecord, acknowledgment);

        Assert.assertEquals(1L, webhookMessage.getRetryCount());
        Mockito.verify(webHookDispatcherService, Mockito.times(1)).dispatch(any());
        Mockito.verify(acknowledgment, Mockito.times(1)).acknowledge();

        Mockito.when(timeDispatchFilter.filter(webhookMessage, 4L)).thenReturn(false);

        ConsumerRecord<String, WebhookMessage> consumerRecord1 = new ConsumerRecord<>("key", 0, 0, "d", webhookMessage);
        lastRetryWebHookListener.onMessage(consumerRecord1, acknowledgment);

        Mockito.verify(consumerSeekCallback, Mockito.times(1)).seek(consumerRecord1.topic(), consumerRecord1.partition(), consumerRecord1.offset());
    }

}