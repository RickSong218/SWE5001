package com.ssms.faraday.core.interceptor;

import com.ssms.faraday.config.MappingProperties;
import com.ssms.faraday.core.http.RequestData;

public interface PreForwardRequestInterceptor {
    void intercept(RequestData data, MappingProperties mapping);
}
