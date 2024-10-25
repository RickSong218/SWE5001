package com.ssms.mail.controller;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ssms.common.api.BaseResponse;
import com.ssms.mail.dto.EmailRequest;
import com.ssms.mail.service.MailSendService;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@Validated
public class MailController {

    private static ILogger logger = SLoggerFactory.getLogger(MailController.class);

    @Autowired
    private MailSendService mailSendService;

    @PostMapping(path = "/send")
    public BaseResponse send(@RequestBody @Valid EmailRequest request) {
        mailSendService.sendMailAsync(request);
        return BaseResponse.builder().message("email has been sent async.").build();
    }


}
