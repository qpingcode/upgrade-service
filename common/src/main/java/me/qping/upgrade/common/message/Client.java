package me.qping.upgrade.common.message;

import me.qping.upgrade.common.message.retry.RetryPolicy;

public interface Client {
    void disconnect();
    void connect();
    RetryPolicy getRetryPolicy();
    long getNodeId();
    long getMessageId();

    boolean isOnline();
    void setOnline(boolean online);
}
