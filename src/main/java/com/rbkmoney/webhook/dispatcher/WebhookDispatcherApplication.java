package com.rbkmoney.webhook.dispatcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class WebhookDispatcherApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebhookDispatcherApplication.class, args);
    }

}
