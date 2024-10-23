package com.SSMS.common.config;

import com.SSMS.common.aop.SentryClientAspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.SSMS.common.error.GlobalExceptionTranslator;

/**
 * Use this common config for Rest API
 */
@Configuration
@Import(value = {SSMSConfig.class, SentryClientAspect.class, GlobalExceptionTranslator.class})
public class SSMSRestConfig {
}
