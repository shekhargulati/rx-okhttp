/*
 * The MIT License
 *
 * Copyright 2015 Shekhar Gulati <shekhargulati84@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.shekhargulati.reactivex.rxokhttp;

import com.shekhargulati.reactivex.rxokhttp.functions.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

class OkHttpBasedRxHttpClient implements RxHttpClient {

    private final Logger logger = LoggerFactory.getLogger(OkHttpBasedRxHttpClient.class);

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType OCTET = MediaType.parse("application/octet-stream; charset=utf-8");
    public static final MediaType TAR = MediaType.parse("application/tar; charset=utf-8");

    private final DefaultOkHttpBasedRxHttpClient client;

    OkHttpBasedRxHttpClient(final String baseApiUrl, final ClientConfig clientConfig) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        setClientConfig(clientConfig, clientBuilder);
        client = new DefaultOkHttpBasedRxHttpClient(baseApiUrl, clientBuilder.build(), RxHttpClient::fullEndpointUrl);
    }

    OkHttpBasedRxHttpClient(final String host, final int port, ClientConfig clientConfig) {
        this(host, port, Optional.empty(), clientConfig);
    }

    OkHttpBasedRxHttpClient(final String host, final int port, final Optional<String> certPath, ClientConfig clientConfig) {
        final String scheme = certPath.isPresent() ? "https" : "http";
        String baseApiUrl = scheme + "://" + host + ":" + port;
        logger.info("Base API uri {}", baseApiUrl);
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (certPath.isPresent()) {
            clientBuilder.sslSocketFactory(new SslCertificates(Paths.get(certPath.get())).sslContext().getSocketFactory());
        }
        setClientConfig(clientConfig, clientBuilder);
        client = new DefaultOkHttpBasedRxHttpClient(baseApiUrl, clientBuilder.build(), RxHttpClient::fullEndpointUrl);
    }

    private void setClientConfig(ClientConfig clientConfig, OkHttpClient.Builder clientBuilder) {
        clientBuilder.followRedirects(clientConfig.isFollowRedirects());
        clientBuilder.followSslRedirects(clientConfig.isFollowSslRedirects());
        clientBuilder.retryOnConnectionFailure(clientConfig.isRetryOnConnectionFailure());
        Duration readTimeout = clientConfig.getReadTimeout();
        if (readTimeout != null) {
            clientBuilder.readTimeout(readTimeout.getSeconds(), TimeUnit.SECONDS);
        }
        Duration writeTimeout = clientConfig.getWriteTimeout();
        if (writeTimeout != null) {
            clientBuilder.writeTimeout(writeTimeout.getSeconds(), TimeUnit.SECONDS);
        }
        Duration connectTimeout = clientConfig.getConnectTimeout();
        if (connectTimeout != null) {
            clientBuilder.connectTimeout(connectTimeout.getSeconds(), TimeUnit.SECONDS);
        }
    }


    @Override
    public Observable<String> get(String endpoint, QueryParameter... queryParameters) {
        return client.get(endpoint, queryParameters);
    }

    @Override
    public Observable<String> get(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.get(endpoint, headers, queryParameters);
    }

    @Override
    public <R> Observable<R> get(String endpoint, StringResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.get(endpoint, transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> get(String endpoint, Map<String, String> headers, StringResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.get(endpoint, headers, transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> get(String endpoint, StringResponseToCollectionTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.get(endpoint, transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> get(String endpoint, Map<String, String> headers, StringResponseToCollectionTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.get(endpoint, headers, transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> get(String endpoint, ResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.get(endpoint, transformer, queryParameters);
    }

    @Override
    public Observable<String> getResponseStream(String endpoint, QueryParameter... queryParameters) {
        return client.getResponseStream(endpoint, queryParameters);
    }

    @Override
    public Observable<String> getResponseStream(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.getResponseStream(endpoint, headers, queryParameters);
    }

    @Override
    public <T> Observable<T> getResponseStream(String endpoint, Map<String, String> headers, StringResponseTransformer<T> transformer, QueryParameter... queryParameters) {
        return client.getResponseStream(endpoint, headers, transformer, queryParameters);
    }

    @Override
    public Observable<Buffer> getResponseBufferStream(String endpoint, QueryParameter... queryParameters) {
        return client.getResponseBufferStream(endpoint, queryParameters);
    }

    @Override
    public <T> Observable<T> getResponseStream(String endpoint, StringResponseTransformer<T> transformer, QueryParameter... queryParameters) {
        return client.getResponseStream(endpoint, transformer, queryParameters);
    }

    @Override
    public Observable<HttpStatus> getResponseHttpStatus(String endpointPath, QueryParameter... queryParameters) {
        return client.getResponseHttpStatus(endpointPath, queryParameters);
    }

    @Override
    public Observable<HttpStatus> post(String endpoint, QueryParameter... queryParameters) {
        return client.post(endpoint, queryParameters);
    }

    @Override
    public Observable<HttpStatus> post(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.post(endpoint, headers, queryParameters);
    }

    @Override
    public Observable<HttpStatus> post(String endpoint, String body, QueryParameter... queryParameters) {
        return client.post(endpoint, body, queryParameters);
    }

    @Override
    public Observable<HttpStatus> post(String endpoint, Map<String, String> headers, String body, QueryParameter... queryParameters) {
        return client.post(endpoint, headers, body, queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters) {
        return client.post(endpoint, bodyTransformer, queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, String postBody, ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters) {
        return client.post(endpoint, postBody, bodyTransformer, queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, Map<String, String> headers, ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters) {
        return client.post(endpoint, headers, bodyTransformer, queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, String postBody, ResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.post(endpoint, postBody, transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, Map<String, String> headers, String postBody, ResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.post(endpoint, headers, postBody, transformer, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveResponse(String endpoint, QueryParameter... queryParameters) {
        return client.postAndReceiveResponse(endpoint, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.postAndReceiveResponse(endpoint, headers, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, Predicate<String> errorChecker, QueryParameter... queryParameters) {
        return client.postAndReceiveResponse(endpoint, headers, errorChecker, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, String postBody, Predicate<String> errorChecker, QueryParameter... queryParameters) {
        return client.postAndReceiveResponse(endpoint, headers, postBody, errorChecker, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveStream(String endpoint, String postBody, QueryParameter... queryParameters) {
        return client.postAndReceiveStream(endpoint, postBody, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveStream(String endpoint, Map<String, String> headers, String postBody, QueryParameter... queryParameters) {
        return client.postAndReceiveStream(endpoint, headers, postBody, queryParameters);
    }

    @Override
    public <R> Observable<R> postTarStream(String endpoint, Path pathToTarArchive, BufferTransformer<R> transformer) {
        return client.postTarStream(endpoint, pathToTarArchive, transformer);
    }

    @Override
    public <R> Observable<R> postTarStream(String endpoint, Path pathToTarArchive, ResponseTransformer<R> transformer) {
        return client.postTarStream(endpoint, pathToTarArchive, transformer);
    }

    @Override
    public Observable<HttpStatus> postTarStream(String endpoint, Path pathToTarArchive) {
        return client.postTarStream(endpoint, pathToTarArchive);
    }

    @Override
    public Observable<HttpStatus> delete(String endpoint, QueryParameter... queryParameters) {
        return client.delete(endpoint, queryParameters);
    }

    @Override
    public Observable<HttpStatus> delete(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.delete(endpoint, headers, queryParameters);
    }

    @Override
    public Observable<Response> head(String endpoint, QueryParameter... queryParameters) {
        return client.head(endpoint, queryParameters);
    }

    @Override
    public Observable<Response> head(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.head(endpoint, headers, queryParameters);
    }
}