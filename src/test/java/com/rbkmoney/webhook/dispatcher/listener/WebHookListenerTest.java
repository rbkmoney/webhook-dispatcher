package com.rbkmoney.webhook.dispatcher.listener;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.dao.WebHookDao;
import com.rbkmoney.webhook.dispatcher.filter.DeadRetryDispatchFilter;
import com.rbkmoney.webhook.dispatcher.filter.PostponedDispatchFilter;
import com.rbkmoney.webhook.dispatcher.handler.WebHookHandlerImpl;
import com.rbkmoney.webhook.dispatcher.service.WebHookDispatcherService;
import com.rbkmoney.webhook.dispatcher.service.WebHookDispatcherServiceImpl;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;

public class WebHookListenerTest {

    WebHookListener webHookListener;
    @Mock
    private WebHookDao webHookDaoImpl;
    @Mock
    private KafkaTemplate<String, WebhookMessage> kafkaTemplate;
    @Mock
    private Acknowledgment acknowledgment;
    @Mock
    private ListenableFuture<SendResult<String, WebhookMessage>> result;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        WebHookDispatcherService webHookDispatcherService = new WebHookDispatcherServiceImpl(HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(60000)
                        .setConnectionRequestTimeout(60000)
                        .setSocketTimeout(60000)
                        .build())
                .build());
        webHookListener = new WebHookListener(new WebHookHandlerImpl(webHookDispatcherService, new PostponedDispatchFilter(webHookDaoImpl),
                new DeadRetryDispatchFilter(webHookDaoImpl), webHookDaoImpl, kafkaTemplate));
    }

    @Test
    public void listen() {

        Mockito.when(webHookDaoImpl.isCommitted(any())).thenReturn(false);
        Mockito.when(kafkaTemplate.send(any(), any(), any())).thenReturn(result);

        WebhookMessage webhookMessage = new WebhookMessage();
        webhookMessage.setUrl("https://webhook.site/e312eefc-54fc-4bca-928e-26f0fc95fc80");
        webhookMessage.setCreatedAt("2019-08-29T10:16:51.804170Z");
        webhookMessage.setSourceId("547839");
        HashMap<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put("Content-Signature", "alg=RS256");
        additionalHeaders.put("digest", "AKIYypDF5jWuNT4aO6OvsWNuzS7e1ztUEVmLwSwaq2Q4j2ckwVJxxz6L1nFQbWZr9Bh8p-hkuKf7MhKZlOKLkhClzDseW-GpJpyhrGnzFHFO78dxbjB8Z82zC5CVJk8PZa-ZxZ2MvoQWTAsPPWVXxJ64A7_tgYiIrSZkjyROwraj1-MG0iRA_a9bkXiwRelNj8mZIv38PneVPl1UAwpMaGs7pQmwaBv-M64Jm8rTd80WiRdOkp8G_hwPQdFo9lOhOxtUk9K5SoBfjKXQ0Dku7X2TpKfTQxHfB1mqm9L8DkK0NXopowqtZI4UB7TTFUIOOSI3SGpv3hyC2uYeTvnJWw==");
        webhookMessage.setAdditionalHeaders(additionalHeaders);
        webhookMessage.setContentType("application/json");
        webhookMessage.setParentEventId(-1L);
        webhookMessage.setEventId(5189508L);
        webhookMessage.setWebhookId(1L);
        webhookMessage.setRequestBody("{}".getBytes());

        webHookListener.listen(webhookMessage, acknowledgment);
    }
}