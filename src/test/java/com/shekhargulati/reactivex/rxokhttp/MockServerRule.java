package com.shekhargulati.reactivex.rxokhttp;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.junit.rules.ExternalResource;

import java.io.IOException;

/**
 * Created by shekhargulati on 01/12/15.
 */
public class MockServerRule extends ExternalResource {

    private final MockWebServer mockWebServer = new MockWebServer();

    @Override
    protected void before() throws Throwable {
        mockWebServer.start();
    }

    @Override
    protected void after() {
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {

        }
    }

    public MockWebServer mockWebServer() {
        return mockWebServer;
    }
}
