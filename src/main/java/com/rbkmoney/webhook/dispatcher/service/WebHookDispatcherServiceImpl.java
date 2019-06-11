package com.rbkmoney.webhook.dispatcher.service;

import com.rbkmoney.kafka.common.exception.RetryableException;
import com.rbkmoney.webhook.dispatcher.Webhook;
import com.rbkmoney.webhook.dispatcher.exception.CantRetryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebHookDispatcherServiceImpl implements WebHookDispatcherService {

    private final OkHttpClient httpClient;
    public static final String SIGNATURE_HEADER = "Content-Signature";
    public static final long RESPONSE_MAX_LENGTH = 4096L;

    @Override
    public int dispatch(Webhook webhook) throws IOException {
        log.info("WebHookDispatcherServiceImpl dispatch hook: {} ", webhook);
        RequestBody body = RequestBody.create(MediaType.parse(webhook.content_type), webhook.getRequestBody());
        final Request request = new Request.Builder()
                .url(webhook.getUrl())
//                .addHeader(SIGNATURE_HEADER, "alg=RS256; digest=" + signature)
                .post(body)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            log.info("Response from hook: messageId: {}, code: {}; body: {}", webhook.getSourceId(),
                    response.code(), response.body() != null ? response.peekBody(RESPONSE_MAX_LENGTH).string() : "<empty>");
            if (response.isSuccessful()) {
                return response.code();
            } else if (response.code() == HttpStatus.REQUEST_TIMEOUT.value()) {
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

}
