package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.dao.WebHookDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostponedDispatchFilter implements DispatchFilter {

    private static final long PARENT_NOT_EXIST_ID = -1;
    private final WebHookDao webHookDao;

    @Override
    public boolean filter(WebhookMessage webhookMessage) {
        return webhookMessage.getParentEventId() != PARENT_NOT_EXIST_ID
                && !webHookDao.isParentCommitted(webhookMessage);
    }
}
