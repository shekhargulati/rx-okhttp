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

package com.shekhargulati.reactivex;

import com.shekhargulati.reactivex.rxokhttp.HttpStatus;
import com.shekhargulati.reactivex.rxokhttp.QueryParameter;
import com.shekhargulati.reactivex.rxokhttp.RxHttpClient;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rx.Observable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RxHttpClientTest {

    private final RxHttpClient client = RxHttpClient.newRxClient("http://example.com");

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

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
        MockWebServer mockWebServer = new MockWebServer();

        mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));
        mockWebServer.start();
        HttpUrl url = mockWebServer.url("/contact-me");

        RxHttpClient client = RxHttpClient.newRxClient(url.toString());
        Observable<HttpStatus> statusObservable = client.post("/form", QueryParameter.of("name", "shekhar"));
        HttpStatus httpStatus = statusObservable.toBlocking().first();
        assertThat(httpStatus.code(), equalTo(200));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String path = recordedRequest.getPath();
        assertThat(path, equalTo("/contact-me/form?name=shekhar"));

    }
}
