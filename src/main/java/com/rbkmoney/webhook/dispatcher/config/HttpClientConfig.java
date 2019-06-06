package com.rbkmoney.webhook.dispatcher.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class HttpClientConfig {

    @Bean
    public OkHttpClient httpClient(@Value("${merchant.callback.timeout}") int timeout) {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        return httpBuilder
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

}
