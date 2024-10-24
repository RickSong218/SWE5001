package com.ssms.mail.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ssms.common.api.BaseResponse;
import com.ssms.mail.MailConstant;
import com.ssms.mail.dto.EmailRequest;

import javax.validation.Valid;

@FeignClient(name = MailConstant.SERVICE_NAME, path = "/v1", url = "${ssms.email-service-endpoint}")
public interface MailClient {
    @PostMapping(path = "/send")
    BaseResponse send(@RequestBody @Valid EmailRequest request);
}
