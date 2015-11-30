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

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rx.Observable;

import java.time.Duration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class RxHttpClientTest {

    private final RxHttpClient client = RxHttpClient.newRxClient("http://example.com");

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Rule
    public MockServerRule mockServerRule = new MockServerRule();

    @Test
    public void shouldThrowExceptionWhenEndpointIsNull() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(is(equalTo("endpoint can't be null or empty.")));
        client.get(null);
    }

    @Test
    public void shouldThrowExceptionWhenEndpointIsEmpty() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(is(equalTo("endpoint can't be null or empty.")));
        client.get("");
    }

    @Test
    public void shouldThrowExceptionWhenEndpointHasOnlySpaces() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(is(equalTo("endpoint can't be null or empty.")));
        client.get("    ");
    }

    @Test
    public void shouldResolveFullEndpointUrlWithQueryParameter() throws Exception {
        String fullEndpointUrl = RxHttpClient.fullEndpointUrl("http://example.com/", "about-me", QueryParameter.of("msg", "hello"));
        assertThat(fullEndpointUrl, is(equalTo("http://example.com/about-me?msg=hello")));
    }

    @Test
    public void shouldResolveFullEndpointUrlWithQueryParameters() throws Exception {
        String fullEndpointUrl = RxHttpClient.fullEndpointUrl("http://example.com", "about-me", QueryParameter.of("msg", "hello"), QueryParameter.of("date", "today"), QueryParameter.of("sortBy", "name"));
        assertThat(fullEndpointUrl, is(equalTo("http://example.com/about-me?msg=hello&date=today&sortBy=name")));
    }

    @Test
    public void shouldResolveFullEndpointUrlWithQueryParametersAsNull() throws Exception {
        QueryParameter[] queryParameters = null;
        String fullEndpointUrl = RxHttpClient.fullEndpointUrl("http://example.com", "about-me", queryParameters);
        assertThat(fullEndpointUrl, is(equalTo("http://example.com/about-me")));
    }

    @Test
    public void shouldResolveFullEndpointUrlWithBaseUrlAndEndpointOnly() throws Exception {
        String fullEndpointUrl = RxHttpClient.fullEndpointUrl("http://example.com", "about-me");
        assertThat(fullEndpointUrl, is(equalTo("http://example.com/about-me")));
    }

    @Test
    public void shouldMakeAPostRequestWithQueryParameters() throws Exception {
        MockWebServer mockWebServer = mockServerRule.mockWebServer();

        MockResponse response = new MockResponse();
        mockWebServer.enqueue(response.setStatus("HTTP/1.1 200 OK"));

        HttpUrl url = mockWebServer.url("/contact-me");

        RxHttpClient client = RxHttpClient.newRxClient(url.toString());
        Observable<HttpStatus> statusObservable = client.post("/form", QueryParameter.of("name", "shekhar"));
        HttpStatus httpStatus = statusObservable.toBlocking().first();
        assertThat(httpStatus.code(), equalTo(200));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertThat(path, equalTo("/contact-me/form?name=shekhar"));
    }

    @Test
    public void shouldMakeAPostAndReceiveResponseRequestWithQueryParameters() throws Exception {
        MockWebServer mockWebServer = mockServerRule.mockWebServer();

        MockResponse response = new MockResponse().setBody("hello");

        mockWebServer.enqueue(response);

        HttpUrl url = mockWebServer.url("/contact-me");

        RxHttpClient client = RxHttpClient.newRxClient(url.toString());

        Observable<String> statusObservable = client.postAndReceiveResponse("/form", QueryParameter.of("name", "shekhar"));
        assertThat(statusObservable.toBlocking().first(), equalTo("hello"));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertThat(path, equalTo("/contact-me/form?name=shekhar"));
        assertThat(recordedRequest.getMethod(), equalTo("POST"));
    }

    @Test
    public void shouldCreateClientInstanceUsingConfiguration() throws Exception {
        ClientConfigBuilder configBuilder = new ClientConfigBuilder()
                .setConnectTimeout(Duration.ofSeconds(30))
                .setWriteTimeout(Duration.ofMinutes(2))
                .setRetryOnConnectionFailure(false)
                .setReadTimeout(Duration.ofHours(1));

        ClientConfig clientConfig = configBuilder.createClientConfig();

        OkHttpBasedRxHttpClient rxHttpClient = (OkHttpBasedRxHttpClient) RxHttpClient.newRxClient("http://google.com", clientConfig);
        OkHttpClient client = rxHttpClient.getClient();
        assertTrue(client.getFollowRedirects());
        assertTrue(client.getFollowSslRedirects());
        assertFalse(client.getRetryOnConnectionFailure());
        assertThat((long) client.getConnectTimeout(), equalTo(Duration.ofSeconds(30).toMillis()));
        assertThat((long) client.getWriteTimeout(), equalTo(Duration.ofMinutes(2).toMillis()));
        assertThat((long) client.getReadTimeout(), equalTo(Duration.ofHours(1).toMillis()));
    }

    @Test
    public void shouldMakeHeadRequestWithoutQueryParameter() throws Exception {
        MockWebServer mockWebServer = mockServerRule.mockWebServer();

        MockResponse response = new MockResponse().setHeader("key", "value");

        mockWebServer.enqueue(response);

        HttpUrl url = mockWebServer.url("/head");

        RxHttpClient client = RxHttpClient.newRxClient(url.toString());

        Observable<Response> responseObservable = client.head("/abc");
        assertThat(responseObservable.toBlocking().first().header("key"), equalTo("value"));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertThat(path, equalTo("/head/abc"));
        assertThat(recordedRequest.getMethod(), equalTo("HEAD"));
    }

    @Test
    public void shouldMakeHeadRequestWithQueryParameter() throws Exception {
        MockWebServer mockWebServer = mockServerRule.mockWebServer();

        MockResponse response = new MockResponse().setHeader("key", "value");

        mockWebServer.enqueue(response);

        HttpUrl url = mockWebServer.url("/head");

        RxHttpClient client = RxHttpClient.newRxClient(url.toString());

        Observable<Response> responseObservable = client.head("/abc", QueryParameter.of("name", "test"));
        assertThat(responseObservable.toBlocking().first().header("key"), equalTo("value"));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertThat(path, equalTo("/head/abc?name=test"));
        assertThat(recordedRequest.getMethod(), equalTo("HEAD"));
    }
}
