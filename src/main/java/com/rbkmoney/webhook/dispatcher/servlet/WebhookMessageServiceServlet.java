package com.rbkmoney.webhook.dispatcher.servlet;

import com.rbkmoney.webhook.dispatcher.WebhookMessageServiceSrv;
import com.rbkmoney.webhook.dispatcher.handler.WebhookMessageServiceHandler;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;

import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/webhook-message-service")
public class WebhookMessageServiceServlet extends GenericServlet {

    private final WebhookMessageServiceHandler handler;

    private Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servlet = new THServiceBuilder()
                .build(WebhookMessageServiceSrv.Iface.class, handler);
    }

    @Override
    public void service(
            ServletRequest request,
            ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }
}
