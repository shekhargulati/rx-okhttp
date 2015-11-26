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
import com.squareup.okhttp.*;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

class OkHttpBasedRxHttpClient implements RxHttpClient {

    private final Logger logger = LoggerFactory.getLogger(OkHttpBasedRxHttpClient.class);

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType OCTET = MediaType.parse("application/octet-stream; charset=utf-8");
    public static final MediaType TAR = MediaType.parse("application/tar; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final String baseApiUrl;

    public OkHttpBasedRxHttpClient(String baseApiUrl) {
        this.baseApiUrl = baseApiUrl;
    }

    OkHttpBasedRxHttpClient(final String host, final int port) {
        this(host, port, Optional.empty());
    }

    OkHttpBasedRxHttpClient(final String host, final int port, final Optional<String> certPath) {
        final String scheme = certPath.isPresent() ? "https" : "http";
        baseApiUrl = scheme + "://" + host + ":" + port;
        logger.info("Base API uri {}", baseApiUrl);
        if (certPath.isPresent()) {
            client.setSslSocketFactory(new SslCertificates(Paths.get(certPath.get())).sslContext().getSocketFactory());
        }
        client.setFollowRedirects(true);
        client.setFollowSslRedirects(true);
    }

    @Override
    public Observable<String> get(final String endpoint, QueryParameter... queryParameters) {
        return get(endpoint, StringResponseTransformer.identityOp(), queryParameters);
    }

    @Override
    public Observable<String> get(final String endpoint, final Map<String, String> headers, QueryParameter... queryParameters) {
        return get(endpoint, headers, StringResponseTransformer.identityOp(), queryParameters);
    }

    @Override
    public <R> Observable<R> get(final String endpoint, final StringResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return get(endpoint, Collections.emptyMap(), transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> get(final String endpoint, final Map<String, String> headers, final StringResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return get(endpoint, headers, transformer.toCollectionTransformer(), queryParameters);
    }

    @Override
    public <R> Observable<R> get(final String endpoint, final StringResponseToCollectionTransformer<R> transformer, QueryParameter... queryParameters) {
        return get(endpoint, Collections.emptyMap(), transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> get(final String endpoint, final Map<String, String> headers, final StringResponseToCollectionTransformer<R> transformer, QueryParameter... queryParameters) {
        Optional.ofNullable(endpoint).map(String::trim).filter(ep -> ep.length() > 0).orElseThrow(() -> new IllegalArgumentException("endpoint can't be null or empty."));
        final String fullEndpointUrl = RxHttpClient.fullEndpointUrl(baseApiUrl, endpoint, queryParameters);
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()) {
                try {
                    Response response = makeHttpGetRequest(fullEndpointUrl, headers);
                    if (response.isSuccessful() && !subscriber.isUnsubscribed()) {
                        try (ResponseBody body = response.body()) {
                            Collection<R> collection = transformer.apply(body.string());
                            collection.forEach(subscriber::onNext);
                            subscriber.onCompleted();
                        }
                    } else if (response.isSuccessful()) {
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new ServiceException(String.format("Service returned %d with message %s", response.code(), response.message()), response.code(), response.message()));
                    }
                } catch (IOException e) {
                    logger.error("Encountered error while making HTTP GET call to '{}'", fullEndpointUrl, e);
                    subscriber.onError(new ServiceException(e));
                }
            }
        });
    }

    @Override
    public <T> Observable<T> getResponseStream(final String endpoint, final StringResponseTransformer<T> transformer, QueryParameter... queryParameters) {
        return getResponseStream(endpoint, Collections.emptyMap(), transformer, queryParameters);
    }

    @Override
    public <T> Observable<T> getResponseStream(final String endpoint, final Map<String, String> headers, final StringResponseTransformer<T> transformer, QueryParameter... queryParameters) {
        final String fullEndpointUrl = RxHttpClient.fullEndpointUrl(baseApiUrl, endpoint, queryParameters);
        return Observable.create(subscriber -> {
            try {
                Response response = makeHttpGetRequest(fullEndpointUrl, headers);
                if (response.isSuccessful() && !subscriber.isUnsubscribed()) {
                    try (ResponseBody body = response.body()) {
                        BufferedSource source = body.source();
                        while (!source.exhausted() && !subscriber.isUnsubscribed()) {
                            subscriber.onNext(transformer.apply(source.buffer().readUtf8()));
                        }
                        subscriber.onCompleted();
                    }
                } else if (response.isSuccessful()) {
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new ServiceException(String.format("Service returned %d with message %s", response.code(), response.message()), response.code(), response.message()));
                }
            } catch (IOException e) {
                logger.error("Encountered error while making {} call", endpoint, e);
                subscriber.onError(new ServiceException(e));
            }
        });
    }

    @Override
    public Observable<String> getResponseStream(final String endpoint, final Map<String, String> headers, QueryParameter... queryParameters) {
        return getResponseStream(endpoint, headers, StringResponseTransformer.identityOp(), queryParameters);
    }

    @Override
    public Observable<Buffer> getResponseBufferStream(final String endpoint, QueryParameter... queryParameters) {
        final String fullEndpointUrl = RxHttpClient.fullEndpointUrl(baseApiUrl, endpoint, queryParameters);
        return Observable.create(subscriber -> {
            try {
                Response response = makeHttpGetRequest(fullEndpointUrl);
                if (response.isSuccessful() && !subscriber.isUnsubscribed()) {
                    try (ResponseBody body = response.body()) {
                        BufferedSource source = body.source();
                        while (!source.exhausted() && !subscriber.isUnsubscribed()) {
                            subscriber.onNext(source.buffer());
                        }
                        subscriber.onCompleted();
                    }
                } else if (response.isSuccessful()) {
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new ServiceException(String.format("Service returned %d with message %s", response.code(), response.message()), response.code(), response.message()));
                }
            } catch (IOException e) {
                logger.error("Encountered error while making {} call", endpoint, e);
                subscriber.onError(new ServiceException(e));
            }
        });
    }

    @Override
    public Observable<String> getResponseStream(final String endpoint, QueryParameter... queryParameters) {
        return getResponseStream(endpoint, Collections.emptyMap(), queryParameters);
    }

    @Override
    public Observable<HttpStatus> getResponseHttpStatus(final String endpointPath, QueryParameter... queryParameters) {
        return get(endpointPath, ResponseTransformer.httpStatus(), queryParameters);
    }

    @Override
    public <R> Observable<R> get(final String endpoint, final ResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        final String fullEndpointUrl = RxHttpClient.fullEndpointUrl(baseApiUrl, endpoint, queryParameters);
        return Observable.create(subscriber -> {
            try {
                Response response = makeHttpGetRequest(fullEndpointUrl);
                if (response.isSuccessful() && !subscriber.isUnsubscribed()) {
                    subscriber.onNext(transformer.apply(response));
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new ServiceException(String.format("Service returned %d with message %s", response.code(), response.message()), response.code(), response.message()));
                }
            } catch (IOException e) {
                logger.error("Encountered error while making {} call", endpoint, e);
                subscriber.onError(new ServiceException(e));
            }
        });
    }

    @Override
    public Observable<HttpStatus> post(final String endpoint, QueryParameter... queryParameters) {
        return post(endpoint, EMPTY_BODY, queryParameters);
    }

    @Override
    public Observable<HttpStatus> post(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return post(endpoint, headers, EMPTY_BODY, queryParameters);
    }

    @Override
    public Observable<HttpStatus> post(final String endpoint, String body, QueryParameter... queryParameters) {
        return post(endpoint, body, ResponseTransformer.httpStatus(), queryParameters);
    }

    @Override
    public Observable<HttpStatus> post(final String endpoint, final Map<String, String> headers, final String body, QueryParameter... queryParameters) {
        return post(endpoint, headers, body, ResponseTransformer.httpStatus(), queryParameters);
    }

    @Override
    public <R> Observable<R> post(final String endpoint, final ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters) {
        return post(endpoint, EMPTY_BODY, ResponseTransformer.fromBody(bodyTransformer), queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, Map<String, String> headers, ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters) {
        return post(endpoint, EMPTY_BODY, bodyTransformer, queryParameters);
    }

    @Override
    public <R> Observable<R> post(final String endpoint, final String postBody, final ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters) {
        return post(endpoint, postBody, ResponseTransformer.fromBody(bodyTransformer), queryParameters);
    }

    @Override
    public <R> Observable<R> post(final String endpoint, final String postBody, final ResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return post(endpoint, Collections.emptyMap(), postBody, transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, Map<String, String> headers, String postBody, ResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        final String fullEndpointUrl = RxHttpClient.fullEndpointUrl(baseApiUrl, endpoint, queryParameters);
        return Observable.create(subscriber -> {
            try {
                Response response = makeHttpPostRequest(fullEndpointUrl, headers, postBody);
                if (response.isSuccessful() && !subscriber.isUnsubscribed()) {
                    subscriber.onNext(transformer.apply(response));
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new ServiceException(String.format("Service returned %d with message %s", response.code(), response.message()), response.code(), response.message()));
                }
            } catch (IOException e) {
                logger.error("Encountered error while making {} call", endpoint, e);
                subscriber.onError(new ServiceException(e));
            }
        });
    }

    @Override
    public Observable<String> postAndReceiveResponse(final String endpoint, QueryParameter... queryParameters) {
        return postAndReceiveResponse(endpoint, Collections.emptyMap(), EMPTY_BODY, t -> false);
    }

    @Override
    public Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return postAndReceiveResponse(endpoint, headers, EMPTY_BODY, t -> false);
    }

    @Override
    public Observable<String> postAndReceiveResponse(final String endpoint, Map<String, String> headers, Predicate<String> errorChecker, QueryParameter... queryParameters) {
        return postAndReceiveResponse(endpoint, headers, EMPTY_BODY, errorChecker);
    }

    @Override
    public Observable<String> postAndReceiveResponse(final String endpoint, Map<String, String> headers, final String postBody, Predicate<String> errorChecker, QueryParameter... queryParameters) {
        final String fullEndpointUrl = RxHttpClient.fullEndpointUrl(baseApiUrl, endpoint, queryParameters);
        return Observable.create(subscriber -> {
            try {
                RequestBody requestBody = new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return OCTET;
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {
                        logger.info("inside request body");
                    }
                };
                Request.Builder requestBuilder = new Request.Builder()
                        .header("Content-Type", "application/json");
                Request postRequest = requestBuilder
                        .url(fullEndpointUrl)
                        .headers(Headers.of(headers))
                        .post(requestBody)
                        .build();
                logger.info("Making POST request to {}", fullEndpointUrl);
                Call call = client.newCall(postRequest);
                Response response = call.execute();
                logger.debug("Received response with code '{}' and headers '{}'", response.code(), response.headers());
                if (response.isSuccessful() && !subscriber.isUnsubscribed()) {
                    try (ResponseBody body = response.body()) {
                        BufferedSource source = body.source();
                        while (!source.exhausted() && !subscriber.isUnsubscribed()) {
                            final String responseLine = source.buffer().readUtf8();
                            if (!errorChecker.test(responseLine)) {
                                subscriber.onNext(responseLine);
                            } else {
                                subscriber.onError(new StreamResponseException(responseLine));
                            }
                        }
                        subscriber.onCompleted();
                    }
                } else {
                    subscriber.onError(new ServiceException(String.format("Service returned %d with message %s", response.code(), response.message()), response.code(), response.message()));
                }
            } catch (Exception e) {
                logger.error("Encountered error while making {} call", endpoint, e);
                subscriber.onError(new ServiceException(e));
            }
        });
    }

    @Override
    public <R> Observable<R> postTarStream(final String endpoint, final Path pathToTarArchive, final BufferTransformer<R> transformer) {

        final RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return TAR;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (FileInputStream fin = new FileInputStream(pathToTarArchive.toFile())) {
                    final byte[] buffer = new byte[1024];
                    int n;
                    while (-1 != (n = fin.read(buffer))) {
                        sink.write(buffer, 0, n);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Unable to read tar at %s", pathToTarArchive.toAbsolutePath()), e);
                }
            }
        };

        final String fullEndpointUrl = RxHttpClient.fullEndpointUrl(baseApiUrl, endpoint);
        return Observable.create(subscriber ->
                {
                    try {
                        Response response = makeHttpPostRequest(fullEndpointUrl, Collections.emptyMap(), requestBody);
                        if (response.isSuccessful() && !subscriber.isUnsubscribed()) {
                            try (ResponseBody body = response.body()) {
                                BufferedSource source = body.source();
                                while (!source.exhausted() && !subscriber.isUnsubscribed()) {
                                    subscriber.onNext(transformer.apply(source.buffer()));
                                }
                                subscriber.onCompleted();
                            }
                        } else if (response.isSuccessful()) {
                            subscriber.onCompleted();
                        } else {
                            subscriber.onError(new ServiceException(String.format("Service returned %d with message %s", response.code(), response.message()), response.code(), response.message()));
                        }
                    } catch (IOException e) {
                        logger.error("Encountered error while making {} call", endpoint, e);
                        subscriber.onError(new ServiceException(e));
                    }
                }
        );
    }

    @Override
    public Observable<HttpStatus> delete(final String endpoint) {
        return delete(endpoint, Collections.emptyMap());
    }

    @Override
    public Observable<HttpStatus> delete(String endpoint, Map<String, String> headers) {
        final String fullEndpointUrl = RxHttpClient.fullEndpointUrl(baseApiUrl, endpoint);
        return Observable.create(subscriber -> {
            try {
                Response response = makeHttpDeleteRequest(fullEndpointUrl, headers);
                if (response.isSuccessful()) {
                    subscriber.onNext(HttpStatus.of(response.code(), response.message()));
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new ServiceException(String.format("Service returned %d with message %s", response.code(), response.message()), response.code(), response.message()));
                }
            } catch (IOException e) {
                logger.error("Encountered error while making {} call", endpoint, e);
                subscriber.onError(new ServiceException(e));
            }
        });
    }

    private Response makeHttpDeleteRequest(String fullEndpointUrl, Map<String, String> headers) throws IOException {
        Request deleteRequest = new Request.Builder()
                .header("Content-Type", "application/json")
                .headers(Headers.of(headers))
                .url(fullEndpointUrl)
                .delete()
                .build();
        logger.info("Making DELETE request to {}", fullEndpointUrl);
        Call call = client.newCall(deleteRequest);
        return call.execute();
    }


    private Response makeHttpGetRequest(final String fullEndpointUrl) throws IOException {
        return makeHttpGetRequest(fullEndpointUrl, Collections.emptyMap());
    }

    private Response makeHttpGetRequest(final String fullEndpointUrl, final Map<String, String> headers) throws IOException {
        Request getRequest = new Request.Builder()
                .url(fullEndpointUrl)
                .headers(Headers.of(headers))
                .build();
        logger.info("Making GET request to {}", fullEndpointUrl);
        Call call = client.newCall(getRequest);
        Response response = call.execute();
        logger.debug("Received response with code '{}' and headers '{}'", response.code(), response.headers());
        return response;
    }


    private Response makeHttpPostRequest(String fullEndpointUrl, Map<String, String> headers, String body) throws IOException {
        RequestBody requestBody = RequestBody.create(JSON, body);
        return makeHttpPostRequest(fullEndpointUrl, headers, requestBody);
    }

    private Response makeHttpPostRequest(final String fullEndpointUrl, Map<String, String> headers, final RequestBody requestBody) throws IOException {
        Request getRequest = new Request.Builder()
                .header("Content-Type", "application/json")
                .headers(Headers.of(headers))
                .url(fullEndpointUrl)
                .post(requestBody)
                .build();
        logger.info("Making POST request to {}", fullEndpointUrl);
        Call call = client.newCall(getRequest);
        return call.execute();
    }
}