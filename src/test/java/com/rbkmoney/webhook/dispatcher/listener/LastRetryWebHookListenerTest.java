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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.Acknowledgment;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LastRetryWebHookListenerTest {

    private static final long DEFAULT_TIMEOUT = 1L;
    private static final String TOPIC = "test";
    private static final String SOURCE_ID = "test";

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
    private WebHookDao webHookDao;
    @Mock
    private ConsumerSeekAware.ConsumerSeekCallback consumerSeekCallback;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        WebHookHandlerImpl handler = new WebHookHandlerImpl(
                webHookDispatcherService,
                postponedDispatchFilter,
                deadRetryDispatchFilter,
                webHookDao,
                kafkaTemplate);

        this.handler = new RetryHandler(handler, timeDispatchFilter);
    }

    @Test
    public void listen() throws IOException {
        LastRetryWebHookListener lastRetryWebHookListener = new LastRetryWebHookListener(DEFAULT_TIMEOUT, TOPIC,
                DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, handler);
        lastRetryWebHookListener.registerSeekCallback(consumerSeekCallback);

        WebhookMessage webhookMessage = new WebhookMessage()
                .setSourceId(SOURCE_ID);
        when(timeDispatchFilter.filter(webhookMessage, 4L)).thenReturn(true);
        when(deadRetryDispatchFilter.filter(webhookMessage)).thenReturn(false);
        when(postponedDispatchFilter.filter(webhookMessage)).thenReturn(false);
        doNothing().when(webHookDao).commit(webhookMessage);

        ConsumerRecord<String, WebhookMessage> consumerRecord = new ConsumerRecord<>(
                "key", 0, 0, "d", webhookMessage);
        lastRetryWebHookListener.onMessage(consumerRecord, acknowledgment);

        assertEquals(1L, webhookMessage.getRetryCount());
        verify(webHookDispatcherService, times(1)).dispatch(any());
        verify(acknowledgment, times(1)).acknowledge();

        when(timeDispatchFilter.filter(webhookMessage, 4L)).thenReturn(false);

        ConsumerRecord<String, WebhookMessage> consumerRecord1 = new ConsumerRecord<>(
                "key", 0, 0, "d", webhookMessage);
        lastRetryWebHookListener.onMessage(consumerRecord1, acknowledgment);

        verify(consumerSeekCallback, times(1))
                .seek(consumerRecord1.topic(), consumerRecord1.partition(), consumerRecord1.offset());
    }

}