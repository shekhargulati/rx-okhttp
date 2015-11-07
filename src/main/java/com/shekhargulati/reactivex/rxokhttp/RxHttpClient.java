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

    /**
     * This method makes an HTTP GET request and return response body as String of Observable
     * The returned Observable will only have a single element.
     *
     * @param endpoint Endpoint at which to make the GET call
     * @return Observable with single String value
     */
    Observable<String> get(String endpoint);

    /**
     * This method makes an HTTP GET request and return response body as String of Observable.
     * The returned Observable will only have a single element.
     *
     * @param endpoint Endpoint at which to make the GET call
     * @param headers  Http headers that you want to pass along
     * @return Observable with single String value
     */
    Observable<String> get(String endpoint, Map<String, String> headers);

    /**
     * This method makes an HTTP GET request and then convert the resultant JSON into R using the StringResponseTransformer function
     *
     * @param endpoint    Endpoint at which to make the GET call
     * @param transformer function to convert String response into some other domain object
     * @param <R>         type returned by StringResponseTransformer
     * @return Observable with single R value
     */
    <R> Observable<R> get(final String endpoint, StringResponseTransformer<R> transformer);

    /**
     * This method makes an HTTP GET request and then convert the resultant JSON into R using the StringResponseTransformer function
     *
     * @param endpoint    Endpoint at which to make the GET call
     * @param headers     HTTP headers to be sent along with the request
     * @param transformer function to convert String response into some other domain object
     * @param <R>         type returned by StringResponseTransformer
     * @return Observable with single R value
     */
    <R> Observable<R> get(String endpoint, Map<String, String> headers, StringResponseTransformer<R> transformer);

    /**
     * This methods makes an HTTP GET request, then convert the result JSON into a Collection using StringResponseToCollectionTransformer, and finally returns an Observable with elements equal to number of elements in the Collection.
     *
     * @param endpoint    Endpoint at which to make the GET call
     * @param transformer function to convert String response into a Collection<R>
     * @param <R>         type to convert to
     * @return Observable with multiple R values
     */
    <R> Observable<R> get(String endpoint, StringResponseToCollectionTransformer<R> transformer);

    /**
     * This methods makes an HTTP GET request, then convert the result JSON into a Collection using StringResponseToCollectionTransformer, and finally returns an Observable with elements equal to number of elements in the Collection.
     *
     * @param endpoint    Endpoint at which to make the GET call
     * @param transformer function to convert String response into a Collection<R>
     * @param headers     HTTP headers to be sent along with the request
     * @param <R>         type to convert to
     * @return Observable with multiple R values
     */
    <R> Observable<R> get(String endpoint, Map<String, String> headers, StringResponseToCollectionTransformer<R> transformer);

    /**
     * This method makes an HTTP GET request and allows users to tranform the OkHttp Response directly.
     *
     * @param endpoint    Endpoint at which to make the GET call
     * @param transformer a function to convert Response to R
     * @param <R>         type to convert to
     * @return Observable with single R value
     */
    <R> Observable<R> get(String endpoint, ResponseTransformer<R> transformer);

    Observable<String> getResponseStream(String endpoint);

    Observable<String> getResponseStream(String endpoint, Map<String, String> headers);

    <T> Observable<T> getResponseStream(String endpoint, Map<String, String> headers, StringResponseTransformer<T> transformer);

    Observable<Buffer> getResponseBufferStream(String endpoint);

    <T> Observable<T> getResponseStream(String endpoint, StringResponseTransformer<T> transformer);

    Observable<HttpStatus> getResponseHttpStatus(String endpointPath);

    Observable<HttpStatus> post(String endpoint);

    Observable<HttpStatus> post(String endpoint, Map<String, String> headers);

    Observable<HttpStatus> post(String endpoint, String body);

    Observable<HttpStatus> post(String endpoint, Map<String, String> headers, String body);

    <R> Observable<R> post(String endpoint, ResponseBodyTransformer<R> bodyTransformer);

    <R> Observable<R> post(String endpoint, String postBody, ResponseBodyTransformer<R> bodyTransformer);

    <R> Observable<R> post(String endpoint, Map<String, String> headers, ResponseBodyTransformer<R> bodyTransformer);

    <R> Observable<R> post(String endpoint, String postBody, ResponseTransformer<R> transformer);

    <R> Observable<R> post(String endpoint, Map<String, String> headers, String postBody, ResponseTransformer<R> transformer);

    Observable<String> postAndReceiveResponse(String endpoint);

    Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers);

    Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, Predicate<String> errorChecker);

    Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, String postBody, Predicate<String> errorChecker);

    <R> Observable<R> postTarStream(String endpoint, Path pathToTarArchive, BufferTransformer<R> transformer);

    Observable<HttpStatus> delete(final String endpoint);

    Observable<HttpStatus> delete(final String endpoint, Map<String, String> headers);

}
