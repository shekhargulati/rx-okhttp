package com.shekhargulati.reactivex.rxokhttp.functions;

import com.shekhargulati.reactivex.rxokhttp.QueryParameter;
import okhttp3.HttpUrl;

public interface HttpUrlFunction {

    HttpUrl apply(String baseApiUrl, String endpoint, QueryParameter... queryParameters);
}
