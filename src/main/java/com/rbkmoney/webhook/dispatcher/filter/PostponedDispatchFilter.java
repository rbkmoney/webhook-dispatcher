package com.rbkmoney.webhook.dispatcher.filter;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import com.rbkmoney.webhook.dispatcher.dao.WebHookDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostponedDispatchFilter implements DispatchFilter {

    private final WebHookDao webHookDao;

    @Override
    public Boolean filter(WebhookMessage webhookMessage) {
        return !webHookDao.isParentCommitted(webhookMessage);
    }
}
