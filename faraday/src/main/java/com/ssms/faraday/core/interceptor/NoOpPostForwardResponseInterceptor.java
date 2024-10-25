package com.ssms.faraday.core.interceptor;

import com.ssms.faraday.config.MappingProperties;
import com.ssms.faraday.core.http.ResponseData;

public class NoOpPostForwardResponseInterceptor implements PostForwardResponseInterceptor {
    @Override
    public void intercept(ResponseData data, MappingProperties mapping) {

    }
}
