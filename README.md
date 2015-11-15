# Reactive OkHttp [![Build Status](https://travis-ci.org/shekhargulati/rx-okhttp.svg)](https://travis-ci.org/shekhargulati/rx-okhttp)

A think RxJava Observable wrapper around OKHttp.


Getting Started
-------------

To use rx-okhttp in your application, you have to add `rx-okhttp` in your classpath. rx-okhttp is available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Crx-okhttp) so you just need to add dependency to your favorite build tool as show below.

For Apache Maven users, please add following to your pom.xml.

```xml
<dependencies>
    <dependency>
        <groupId>com.shekhargulati.reactivex</groupId>
        <artifactId>rx-okhttp</artifactId>
        <version>0.1.0</version>
        <type>jar</type>
    </dependency>
</dependencies>
```

Gradle users can add following to their build.gradle file.

```
compile(group: 'com.shekhargulati.reactivex', name: 'rx-okhttp', version: '0.1.0', ext: 'jar'){
        transitive=true
}
```