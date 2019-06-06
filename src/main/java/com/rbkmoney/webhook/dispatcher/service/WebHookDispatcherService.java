package com.rbkmoney.webhook.dispatcher.service;


import com.rbkmoney.webhook.dispatcher.Webhook;

import java.io.IOException;

public interface WebHookDispatcherService {

    int dispatch(Webhook webhook) throws IOException;

}
