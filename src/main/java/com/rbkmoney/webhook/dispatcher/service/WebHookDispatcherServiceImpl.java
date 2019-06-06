package com.rbkmoney.webhook.dispatcher.service;

import com.rbkmoney.webhook.dispatcher.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
        RequestBody body = RequestBody.create(MediaType.parse(webhook.content_type), "");
        final Request request = new Request.Builder()
                .url(webhook.getUrl())
//                .addHeader(SIGNATURE_HEADER, "alg=RS256; digest=" + signature)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            log.info("Response from hook: messageId: {}, code: {}; body: {}", webhook.getSourceId(),
                    response.code(), response.body() != null ? response.peekBody(RESPONSE_MAX_LENGTH).string() : "<empty>");
            return response.code();
        }
    }

}
