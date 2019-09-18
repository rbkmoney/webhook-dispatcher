package com.rbkmoney.webhook.dispatcher.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.rbkmoney.kafka.common.exception.RetryableException;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.shaded.org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

public class WebHookDispatcherServiceImplTest {

    public static final String ALG_RS_256 = "alg=RS256";
    public static final String DIGEST = "oOg_wGfM3esi5aAmu4fnik6DRISvusM2r99i7iyQapkL_5Q30riAD6jSr9LOearJea6053JjodQ7vVIPsTDb1pnZ4thSe7qLU_JzyL_q-LCQXWyGVBXpIyt5fN-1yRNr-Bl1hpnmc5JpNWuNvZdqpoPkvrW4vaNUmLgXqgtpgyHIxQDMZVLnAmzXBCvWggqORPpZ_6J1oNbh1QqEBC9CqDU94d8GthzqxH3V7nIPdpYmg8VxbR9k5SGXf8zbIDWxWMzVfKQF4B1B1CtO46loD70cmOX2kMl32WJa_XSV8Ep1ajDnouLyxk4eN-F-Fb1XkUWUJPw0JkKAVhp2F4NxzQ==";
    private CloseableHttpClient httpClient;
    private WebHookDispatcherServiceImpl webHookDispatcherService;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void init() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .setSocketTimeout(1000).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        webHookDispatcherService = new WebHookDispatcherServiceImpl(httpClient);
    }


    @Test(expected = RetryableException.class)
    public void dispatchError() throws IOException {
        stubFor(WireMock.post(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(20000)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("<response>Some content</response>")));

        WebhookMessage webhookMessage = new WebhookMessage();
        webhookMessage.setUrl("http://localhost:8089/test");
        webhookMessage.setRequestBody("{}".getBytes());
        webhookMessage.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        HashMap<String, String> additionalHeaders = new HashMap<>();
        webhookMessage.setAdditionalHeaders(additionalHeaders);
        webHookDispatcherService.dispatch(webhookMessage);
    }

    @Test
    public void dispatch() throws IOException {
        stubFor(WireMock.post(urlEqualTo("/test"))
                .withHeader(CONTENT_TYPE, new EqualToPattern(ContentType.APPLICATION_JSON.getMimeType()))
                .withHeader("Content-Signature", new EqualToPattern(ALG_RS_256))
                .withHeader("digest", new EqualToPattern(DIGEST))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        WebhookMessage webhookMessage = new WebhookMessage();
        webhookMessage.setUrl("http://localhost:8089/test");
        webhookMessage.setRequestBody("{}".getBytes());
        webhookMessage.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        HashMap<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put("Content-Signature", ALG_RS_256);
        additionalHeaders.put("digest", DIGEST);
        webhookMessage.setAdditionalHeaders(additionalHeaders);
        webHookDispatcherService.dispatch(webhookMessage);
    }
}
