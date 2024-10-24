package com.ssms.common.config;

import com.ssms.common.aop.SentryClientAspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.ssms.common.error.GlobalExceptionTranslator;

/**
 * Use this common config for Rest API
 */
@Configuration
@Import(value = {com.ssms.common.config.ssmsConfig.class, SentryClientAspect.class, GlobalExceptionTranslator.class})
public class ssmsRestConfig {
}
