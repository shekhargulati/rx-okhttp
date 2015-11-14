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
import com.shekhargulati.reactivex.rxokhttp.functions.StringResponseToCollectionTransformer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class GithubApiTests {

    final String githubApiUrl = "https://api.github.com";
    private final RxHttpClient client = RxHttpClient.newRxClient(githubApiUrl);
    private String githubUser = "shekhargulati";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void shouldListPublicGithubRepositoriesForAUserAsSingleStringElementObservable() throws Exception {
        final String listUserRepoEndpoint = String.format("/users/%s/repos", githubUser);
        Observable<String> repos = client.get(listUserRepoEndpoint);
        TestSubscriber<String> subscriber = new TestSubscriber<>();
        repos.subscribe(subscriber);
        assertThat(subscriber.getOnNextEvents(), hasSize(1));
        assertThat(subscriber.getOnCompletedEvents(), hasSize(1));
        assertThat(subscriber.getOnErrorEvents(), hasSize(0));
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

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
    public void shouldListPublicGithubRepositoriesForAUserAsMultiElementStringObservable() throws Exception {
        final String listUserRepoEndpoint = String.format("/users/%s/repos", githubUser);
        Observable<String> repos = client.get(listUserRepoEndpoint, (StringResponseToCollectionTransformer<String>) (String json) -> toCollection(json));
        TestSubscriber<String> subscriber = new TestSubscriber<>();
        repos.subscribe(subscriber);
        assertThat(subscriber.getOnNextEvents(), hasSize(30));
        assertThat(subscriber.getOnCompletedEvents(), hasSize(1));
        assertThat(subscriber.getOnErrorEvents(), hasSize(0));
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    private List<String> toCollection(String json) {
        List<String> repos = new Gson().fromJson(json, ArrayList.class);
        return repos;
    }

}