# Reactive OkHttp [![Build Status](https://travis-ci.org/shekhargulati/rx-okhttp.svg)](https://travis-ci.org/shekhargulati/rx-okhttp) [![codecov.io](https://codecov.io/github/shekhargulati/rx-okhttp/coverage.svg?branch=master)](https://codecov.io/github/shekhargulati/rx-okhttp?branch=master) [![](https://img.shields.io/maven-central/v/com.shekhargulati.reactivex/rx-okhttp.svg)](http://search.maven.org/#search%7Cga%7C1%7Crx-okhttp) [![License](https://img.shields.io/:license-mit-blue.svg)](./LICENSE)

A think RxJava Observable wrapper around OKHttp. You can use it to build reactive REST API clients. [rx-docker-client](https://github.com/shekhargulati/rx-docker-client) uses this API.


Getting Started
-------------

To use rx-okhttp in your application, you have to add `rx-okhttp` in your classpath. rx-okhttp is available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Crx-okhttp) so you just need to add dependency to your favorite build tool as show below.

For Apache Maven users, please add following to your pom.xml.

```xml
<dependencies>
    <dependency>
        <groupId>com.shekhargulati.reactivex</groupId>
        <artifactId>rx-okhttp</artifactId>
        <version>0.1.8</version>
    </dependency>
</dependencies>
```

Gradle users can add following to their build.gradle file.

```
compile 'com.shekhargulati.reactivex:rx-okhttp:0.1.8'
```