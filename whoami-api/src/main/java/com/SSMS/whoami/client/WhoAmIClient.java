package com.ssms.whoami.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ssms.common.auth.AuthConstant;
import com.ssms.whoami.WhoAmIConstant;
import com.ssms.whoami.dto.FindWhoAmIResponse;
import com.ssms.whoami.dto.GetIntercomSettingResponse;

@FeignClient(name = WhoAmIConstant.SERVICE_NAME, path = "/v1", url = "${staffjoy.whoami-service-endpoint}")
public interface WhoAmIClient {
    @GetMapping
    FindWhoAmIResponse findWhoAmI(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz);

    @GetMapping(value = "/intercom")
    GetIntercomSettingResponse getIntercomSettings(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz);
}
