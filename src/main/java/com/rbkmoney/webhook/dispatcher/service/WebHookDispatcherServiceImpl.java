package com.rbkmoney.webhook.dispatcher.service;

import com.rbkmoney.kafka.common.exception.RetryableException;
import com.rbkmoney.webhook.dispatcher.Webhook;
import com.rbkmoney.webhook.dispatcher.exception.CantRetryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebHookDispatcherServiceImpl implements WebHookDispatcherService {

    public static final long RESPONSE_MAX_LENGTH = 4096L;

    private final CloseableHttpClient client;

    @Override
    public int dispatch(Webhook webhook) throws IOException {
        log.info("WebHookDispatcherServiceImpl dispatch hook: {} ", webhook);
        HttpPost post = new HttpPost(webhook.getUrl());
        post.setEntity(new ByteArrayEntity(webhook.getRequestBody()));
        post.setHeader(CONTENT_TYPE, webhook.getContentType());
        try (CloseableHttpResponse response = client.execute(post)) {
            webhook.getAdditionalHeaders()
                    .forEach(post::addHeader);
            int statusCode = response.getStatusLine().getStatusCode();
            StringBuilder result = readResponse(response);
            log.info("Response from hook: messageId: {}, code: {}; body: {}", webhook.getSourceId(),
                    statusCode, result != null ? result.toString() : "<empty>");
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                return statusCode;
            } else if (statusCode == HttpStatus.REQUEST_TIMEOUT.value()) {
                log.warn("Timeout error when send webhook: {}", webhook);
                throw new RetryableException(HttpStatus.REQUEST_TIMEOUT.getReasonPhrase());
            } else {
                log.warn("Cant retry error when send webhook: {}", webhook);
                throw new CantRetryException("Cant retry webhook!");
            }
        } catch (SocketTimeoutException e) {
            log.warn("Timeout error when send webhook: {}", webhook);
            throw new RetryableException(e);
        }
    }

    private StringBuilder readResponse(CloseableHttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null && result.length() <= RESPONSE_MAX_LENGTH) {
            result.append(line);
        }
        return result;
    }

}
