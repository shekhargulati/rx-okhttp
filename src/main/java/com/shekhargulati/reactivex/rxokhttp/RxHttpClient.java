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
import okio.Buffer;
import rx.Observable;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RxHttpClient {

    public static final String EMPTY_BODY = "";

    static RxHttpClient newRxClient(final String host, final int port) {
        return new OkHttpBasedRxHttpClient(host, port);
    }

    static RxHttpClient newRxClient(final String host, final int port, String certPath) {
        return newRxClient(host, port, Optional.ofNullable(certPath));
    }

    static RxHttpClient newRxClient(final String host, final int port, Optional<String> certPath) {
        return new OkHttpBasedRxHttpClient(host, port, certPath);
    }

    static RxHttpClient newRxClient(final String apiUrl) {
        return new OkHttpBasedRxHttpClient(apiUrl);
    }

    static String fullEndpointUrl(String baseApiUrl, String endpoint, QueryParameter... queryParameters) throws IllegalArgumentException {
        baseApiUrl = Optional.ofNullable(baseApiUrl)
                .filter(e -> e.trim().length() > 0)
                .map(e -> e.endsWith("/") ? e.substring(0, e.lastIndexOf("/")) : e)
                .orElseThrow(() -> new IllegalArgumentException("baseApiUrl can't be null or empty"));
        endpoint = Optional.ofNullable(endpoint)
                .filter(e -> e.trim().length() > 0)
                .map(e -> e.startsWith("/") ? e : "/" + e)
                .orElseThrow(() -> new IllegalArgumentException("endpoint can't be null or empty"));
        String queryString = Optional.ofNullable(queryParameters).map(qps -> Stream.of(qps).map(qp -> String.format("%s=%s", qp.param(), qp.value())).collect(Collectors.joining("&", "?", ""))).orElse("");
        queryString = queryString.endsWith("?") ? "" : queryString;
        return baseApiUrl + endpoint + queryString;
    }

    /**
     * This method makes an HTTP GET request and return response body as String of Observable
     * The returned Observable will only have a single element.
     *
     * @param endpoint        Endpoint at which to make the GET call
     * @param queryParameters Query parameters that will be use to build the final url
     * @return Observable with single String value
     */
    Observable<String> get(String endpoint, QueryParameter... queryParameters);

    /**
     * This method makes an HTTP GET request and return response body as String of Observable.
     * The returned Observable will only have a single element.
     *
     * @param endpoint        Endpoint at which to make the GET call
     * @param queryParameters Query parameters that will be use to build the final url
     * @param headers         Http headers that you want to pass along
     * @return Observable with single String value
     */
    Observable<String> get(String endpoint, Map<String, String> headers, QueryParameter... queryParameters);

    /**
     * This method makes an HTTP GET request and then convert the resultant JSON into R using the StringResponseTransformer function
     *
     * @param endpoint        Endpoint at which to make the GET call
     * @param transformer     function to convert String response into some other domain object
     * @param queryParameters Query parameters that will be use to build the final url
     * @param <R>             type returned by StringResponseTransformer
     * @return Observable with single R value
     */
    <R> Observable<R> get(final String endpoint, StringResponseTransformer<R> transformer, QueryParameter... queryParameters);

    /**
     * This method makes an HTTP GET request and then convert the resultant JSON into R using the StringResponseTransformer function
     *
     * @param endpoint        Endpoint at which to make the GET call
     * @param headers         HTTP headers to be sent along with the request
     * @param transformer     function to convert String response into some other domain object
     * @param queryParameters Query parameters that will be use to build the final url
     * @param <R>             type returned by StringResponseTransformer
     * @return Observable with single R value
     */
    <R> Observable<R> get(String endpoint, Map<String, String> headers, StringResponseTransformer<R> transformer, QueryParameter... queryParameters);

    /**
     * This methods makes an HTTP GET request, then convert the result JSON into a Collection using StringResponseToCollectionTransformer, and finally returns an Observable with elements equal to number of elements in the Collection.
     *
     * @param endpoint        Endpoint at which to make the GET call
     * @param transformer     function to convert String response into a Collection
     * @param queryParameters Query parameters that will be use to build the final url
     * @param <R>             type to convert to
     * @return Observable with multiple R values
     */
    <R> Observable<R> get(String endpoint, StringResponseToCollectionTransformer<R> transformer, QueryParameter... queryParameters);

    /**
     * This methods makes an HTTP GET request, then convert the result JSON into a Collection using StringResponseToCollectionTransformer, and finally returns an Observable with elements equal to number of elements in the Collection.
     *
     * @param endpoint        Endpoint at which to make the GET call
     * @param transformer     function to convert String response into a Collection
     * @param headers         HTTP headers to be sent along with the request
     * @param queryParameters Query parameters that will be use to build the final url
     * @param <R>             type to convert to
     * @return Observable with multiple R values
     */
    <R> Observable<R> get(String endpoint, Map<String, String> headers, StringResponseToCollectionTransformer<R> transformer, QueryParameter... queryParameters);

    /**
     * This method makes an HTTP GET request and allows users to tranform the OkHttp Response directly.
     *
     * @param endpoint        Endpoint at which to make the GET call
     * @param transformer     a function to convert Response to R
     * @param queryParameters Query parameters that will be use to build the final url
     * @param <R>             type to convert to
     * @return Observable with single R value
     */
    <R> Observable<R> get(String endpoint, ResponseTransformer<R> transformer, QueryParameter... queryParameters);

    Observable<String> getResponseStream(String endpoint, QueryParameter... queryParameters);

    Observable<String> getResponseStream(String endpoint, Map<String, String> headers, QueryParameter... queryParameters);

    <T> Observable<T> getResponseStream(String endpoint, Map<String, String> headers, StringResponseTransformer<T> transformer, QueryParameter... queryParameters);

    Observable<Buffer> getResponseBufferStream(String endpoint, QueryParameter... queryParameters);

    <T> Observable<T> getResponseStream(String endpoint, StringResponseTransformer<T> transformer, QueryParameter... queryParameters);

    Observable<HttpStatus> getResponseHttpStatus(String endpointPath, QueryParameter... queryParameters);

    Observable<HttpStatus> post(String endpoint, QueryParameter... queryParameters);

    Observable<HttpStatus> post(String endpoint, Map<String, String> headers, QueryParameter... queryParameters);

    Observable<HttpStatus> post(String endpoint, String body, QueryParameter... queryParameters);

    Observable<HttpStatus> post(String endpoint, Map<String, String> headers, String body, QueryParameter... queryParameters);

    <R> Observable<R> post(String endpoint, ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters);

    <R> Observable<R> post(String endpoint, String postBody, ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters);

    <R> Observable<R> post(String endpoint, Map<String, String> headers, ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters);

    <R> Observable<R> post(String endpoint, String postBody, ResponseTransformer<R> transformer, QueryParameter... queryParameters);

    <R> Observable<R> post(String endpoint, Map<String, String> headers, String postBody, ResponseTransformer<R> transformer, QueryParameter... queryParameters);

    Observable<String> postAndReceiveResponse(String endpoint, QueryParameter... queryParameters);

    Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, QueryParameter... queryParameters);

    Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, Predicate<String> errorChecker, QueryParameter... queryParameters);

    Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, String postBody, Predicate<String> errorChecker, QueryParameter... queryParameters);

    <R> Observable<R> postTarStream(String endpoint, Path pathToTarArchive, BufferTransformer<R> transformer);

    Observable<HttpStatus> delete(final String endpoint);

    Observable<HttpStatus> delete(final String endpoint, Map<String, String> headers);

}
