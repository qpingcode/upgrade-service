package me.qping.upgrade.common.message.retry;

import java.util.Random;

public class ExponentialBackOffRetry implements RetryPolicy {

    private static final int DEFAULT_MAX_SLEEP_MS = 30 * 60 * 1000;  // 30分钟

    private final Random random = new Random();
    private final long baseSleepTimeMs;
    private final int maxRetries;
    private final int maxSleepMs;

    public ExponentialBackOffRetry(int baseSleepTimeMs, int maxRetries) {
        this(baseSleepTimeMs, maxRetries, DEFAULT_MAX_SLEEP_MS);
    }

    public ExponentialBackOffRetry(int baseSleepTimeMs, int maxRetries, int maxSleepMs) {
        this.maxRetries = maxRetries;
        this.baseSleepTimeMs = baseSleepTimeMs;
        this.maxSleepMs = maxSleepMs;
    }

    @Override
    public boolean allowRetry(int retryCount) {
        if (maxRetries == -1 || retryCount < maxRetries) {
            return true;
        }
        return false;
    }

    @Override
    public long getSleepTimeMs(int retryCount) {
        if (retryCount < 0) {
            throw new IllegalArgumentException("retries count must greater than 0.");
        }
        long sleepMs = baseSleepTimeMs * Math.max(1, random.nextInt(1 << retryCount));
        if (sleepMs > maxSleepMs) {
            System.out.println(String.format("Sleep extension too large (%d). Pinning to %d", sleepMs, maxSleepMs));
            sleepMs = maxSleepMs;
        }
        return sleepMs;
    }

    public static void main(String[] args) {
        Random random = new Random();
        for(int i = 0; i < 20; i++){
            System.out.println(random.nextInt(1 << i));
        }
    }
}