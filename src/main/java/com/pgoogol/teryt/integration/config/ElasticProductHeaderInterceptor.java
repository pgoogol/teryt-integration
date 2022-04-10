package com.pgoogol.teryt.integration.config;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

public class ElasticProductHeaderInterceptor implements HttpResponseInterceptor {

    @Override
    public void process(HttpResponse httpResponse, HttpContext httpContext) {
        httpResponse.addHeader("X-Elastic-Product", "Elasticsearch");
    }
}
