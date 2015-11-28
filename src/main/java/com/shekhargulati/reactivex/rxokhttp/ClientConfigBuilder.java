package com.shekhargulati.reactivex.rxokhttp;

import java.time.Duration;

public class ClientConfigBuilder {
    private boolean followRedirects = true;
    private boolean followSslRedirects = true;
    private Duration readTimeout;
    private Duration writeTimeout;
    private boolean retryOnConnectionFailure = true;
    private Duration connectTimeout;

    public ClientConfigBuilder setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public ClientConfigBuilder setFollowSslRedirects(boolean followSslRedirects) {
        this.followSslRedirects = followSslRedirects;
        return this;
    }

    public ClientConfigBuilder setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public ClientConfigBuilder setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public ClientConfigBuilder setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
        this.retryOnConnectionFailure = retryOnConnectionFailure;
        return this;
    }

    public ClientConfigBuilder setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public ClientConfig createClientConfig() {
        return ClientConfig.createClientConfig(followRedirects, followSslRedirects, readTimeout, writeTimeout, retryOnConnectionFailure, connectTimeout);
    }
}