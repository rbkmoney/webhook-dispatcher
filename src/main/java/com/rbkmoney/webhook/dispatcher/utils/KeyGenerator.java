package com.rbkmoney.webhook.dispatcher.utils;

public class KeyGenerator {

    private static final String DELIMETER = "_";

    public static String generateKey(Long hookId, String sourceId, long eventId) {
        return hookId + DELIMETER + sourceId + DELIMETER + eventId;
    }

}
