package me.qping.upgrade.common.message;

import me.qping.upgrade.common.message.retry.RetryPolicy;

public interface Client {
    void disconnect();
    void connect();
    RetryPolicy getRetryPolicy();
    long getClientId();
    long getMessageId();
}
