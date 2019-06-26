package com.rbkmoney.webhook.dispatcher.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.rbkmoney.kafka.common.exception.RetryableException;
import com.rbkmoney.webhook.dispatcher.Webhook;
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

        Webhook webhook = new Webhook();
        webhook.setUrl("http://localhost:8089/test");
        webhook.setRequestBody("{}".getBytes());
        webhook.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        HashMap<String, String> additionalHeaders = new HashMap<>();
        webhook.setAdditionalHeaders(additionalHeaders);
        webHookDispatcherService.dispatch(webhook);
    }


    @Test
    public void dispatch() throws IOException {
        stubFor(WireMock.post(urlEqualTo("/test"))
                .withHeader(CONTENT_TYPE, new EqualToPattern(ContentType.APPLICATION_JSON.getMimeType()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        Webhook webhook = new Webhook();
        webhook.setUrl("http://localhost:8089/test");
        webhook.setRequestBody("{}".getBytes());
        webhook.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        HashMap<String, String> additionalHeaders = new HashMap<>();
        webhook.setAdditionalHeaders(additionalHeaders);
        webHookDispatcherService.dispatch(webhook);
    }
}