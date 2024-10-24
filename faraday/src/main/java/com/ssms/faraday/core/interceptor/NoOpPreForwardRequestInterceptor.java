package com.ssms.faraday.core.interceptor;

import com.ssms.faraday.config.MappingProperties;
import com.ssms.faraday.core.http.RequestData;

public class NoOpPreForwardRequestInterceptor implements PreForwardRequestInterceptor {
    @Override
    public void intercept(RequestData data, MappingProperties mapping) {

    }
}
