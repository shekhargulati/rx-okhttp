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

import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class GithubApiTests {

    final String githubApiUrl = "https://api.github.com";
    RxHttpClient client = RxHttpClient.newRxClient(githubApiUrl);
    private String githubUser = "shekhargulati";

    @Test
    public void shouldListAllGithubRepositoriesAsJSONForAUser() throws Exception {
        final String listUserRepoEndpoint = String.format("/users/%s/repos", githubUser);
        Observable<String> repos = client.get(listUserRepoEndpoint);
        TestSubscriber<String> subscriber = new TestSubscriber<>();
        repos.subscribe(subscriber);
        assertThat(subscriber.getOnNextEvents(), hasSize(1));
        assertThat(subscriber.getOnCompletedEvents(), hasSize(1));
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    @Test
    public void shouldListAllGithubRepositoriesAsDomainCollectionForAUser() throws Exception {
        final String listUserRepoEndpoint = String.format("/users/%s/repos", githubUser);
        Observable<String> repos = client.get(listUserRepoEndpoint);

    }


}