package com.ssms.faraday.core.interceptor;

import com.ssms.faraday.config.MappingProperties;
import com.ssms.faraday.core.http.ResponseData;

public interface PostForwardResponseInterceptor {
    void intercept(ResponseData data, MappingProperties mapping);
}
