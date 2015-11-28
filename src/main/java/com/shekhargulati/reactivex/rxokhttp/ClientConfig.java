package com.shekhargulati.reactivex.rxokhttp;

import java.time.Duration;

public class ClientConfig {

    private boolean followRedirects = true;
    private boolean followSslRedirects = true;
    private Duration readTimeout = Duration.ZERO;
    private Duration writeTimeout;
    private boolean retryOnConnectionFailure = true;
    private Duration connectTimeout;

    private ClientConfig() {
    }

    private ClientConfig(boolean followRedirects, boolean followSslRedirects, Duration readTimeout, Duration writeTimeout, boolean retryOnConnectionFailure, Duration connectTimeout) {
        this.followRedirects = followRedirects;
        this.followSslRedirects = followSslRedirects;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.retryOnConnectionFailure = retryOnConnectionFailure;
        this.connectTimeout = connectTimeout;
    }

    public static ClientConfig defaultConfig() {
        return new ClientConfig();
    }

    static ClientConfig createClientConfig(boolean followRedirects, boolean followSslRedirects, Duration readTimeout, Duration writeTimeout, boolean retryOnConnectionFailure, Duration connectTimeout) {
        return new ClientConfig(followRedirects, followSslRedirects, readTimeout, writeTimeout, retryOnConnectionFailure, connectTimeout);
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public boolean isFollowSslRedirects() {
        return followSslRedirects;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public boolean isRetryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }
}
