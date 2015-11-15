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

import com.shekhargulati.reactivex.rxokhttp.QueryParameter;
import com.shekhargulati.reactivex.rxokhttp.RxHttpClient;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RxHttpClientTest {

    @Test
    public void shouldResolveFullEndpointUrlWithQueryParameter() throws Exception {
        String fullEndpointUrl = RxHttpClient.fullEndpointUrl("http://shekhargulati.com/", "about-me", QueryParameter.of("msg", "hello"));
        assertThat(fullEndpointUrl, is(equalTo("http://shekhargulati.com/about-me?msg=hello")));
    }

    @Test
    public void shouldResolveFullEndpointUrlWithQueryParameters() throws Exception {
        String fullEndpointUrl = RxHttpClient.fullEndpointUrl("http://shekhargulati.com", "about-me", QueryParameter.of("msg", "hello"), QueryParameter.of("date", "today"), QueryParameter.of("sortBy", "name"));
        assertThat(fullEndpointUrl, is(equalTo("http://shekhargulati.com/about-me?msg=hello&date=today&sortBy=name")));
    }

    @Test
    public void shouldResolveFullEndpointUrlWithQueryParametersAsNull() throws Exception {
        QueryParameter[] queryParameters = null;
        String fullEndpointUrl = RxHttpClient.fullEndpointUrl("http://shekhargulati.com", "about-me", queryParameters);
        assertThat(fullEndpointUrl, is(equalTo("http://shekhargulati.com/about-me")));
    }

    @Test
    public void shouldResolveFullEndpointUrlWithBaseUrlAndEndpointOnly() throws Exception {
        String fullEndpointUrl = RxHttpClient.fullEndpointUrl("http://shekhargulati.com", "about-me");
        assertThat(fullEndpointUrl, is(equalTo("http://shekhargulati.com/about-me")));
    }
}
