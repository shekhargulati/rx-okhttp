package com.shekhargulati.reactivex.rxokhttp;

import org.junit.Ignore;
import rx.Observable;

public class OkHttpBasedRxHttpClientTest {

    private final String baseApiUrl = "https://api.github.com";

    @Ignore
    public void shouldListRepositoriesForAUser() throws Exception {
        RxHttpClient rxHttpClient = RxHttpClient.newRxClient(baseApiUrl);
        Observable<String> observable = rxHttpClient.get("/users/shekhargulati/repos");

        System.out.println(observable.toBlocking().last());
    }
}