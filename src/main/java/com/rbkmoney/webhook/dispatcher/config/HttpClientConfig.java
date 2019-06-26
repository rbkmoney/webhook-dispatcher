package com.rbkmoney.webhook.dispatcher.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class HttpClientConfig {

    @Bean
    public HttpClient httpClient(@Value("${merchant.callback.timeout}") int timeout) {
        HttpClientBuilder httpBuilder = HttpClientBuilder.create();
        return httpBuilder
                .setConnectionTimeToLive(10000L, TimeUnit.MILLISECONDS)
                .build();

    }

}
