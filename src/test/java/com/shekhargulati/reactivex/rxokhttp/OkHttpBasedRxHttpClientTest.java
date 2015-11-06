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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shekhargulati.reactivex.rxokhttp.functions.StringResponseToCollectionTransformer;
import com.shekhargulati.reactivex.rxokhttp.functions.StringResponseTransformer;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class OkHttpBasedRxHttpClientTest {

    final String githubApiUrl = "https://api.github.com";
    private String githubUser = "shekhargulati";

    @Test
    public void shouldMakeGetCallAndReturnObservableOfString() throws Exception {
        String listUserRepo = String.format("/users/%s/repos", githubUser);
        RxHttpClient client = RxHttpClient.newRxClient(githubApiUrl);
        Observable<String> repos = client.get(listUserRepo);
        TestSubscriber<String> subscriber = new TestSubscriber<>();
        repos.subscribe(subscriber);
        assertThat(subscriber.getOnNextEvents(), hasSize(1));
        assertThat(subscriber.getOnCompletedEvents(), hasSize(1));
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    @Test
    public void shouldMakeGetCallAndReturnObservableWithTransformedType() throws Exception {
        String listUserRepo = String.format("/users/%s/repos", githubUser);
        RxHttpClient client = RxHttpClient.newRxClient(githubApiUrl);
        Type type = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        StringResponseTransformer<List<Map<String, Object>>> transformer = json -> new Gson().fromJson(json, type);
        Observable<List<Map<String, Object>>> repos = client.get(listUserRepo, transformer);
        TestSubscriber<List<Map<String, Object>>> subscriber = new TestSubscriber<>();
        repos.subscribe(subscriber);
        assertThat(subscriber.getOnNextEvents(), hasSize(1));
        assertThat(subscriber.getOnCompletedEvents(), hasSize(1));
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    @Test
    public void shouldMakeGetCallAndReturnObservableWithCollectionTransformedType() throws Exception {
        String listUserRepo = String.format("/users/%s/repos", githubUser);
        RxHttpClient client = RxHttpClient.newRxClient(githubApiUrl);
        Type type = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        StringResponseToCollectionTransformer<Map<String, Object>> transformer = json -> new Gson().fromJson(json, type);
        Observable<Map<String, Object>> repos = client.get(listUserRepo, transformer);
        TestSubscriber<Map<String, Object>> subscriber = new TestSubscriber<>();
        repos.subscribe(subscriber);
        assertThat(subscriber.getOnNextEvents(), hasSize(greaterThanOrEqualTo(30)));
        assertThat(subscriber.getOnCompletedEvents(), hasSize(1));
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }
}