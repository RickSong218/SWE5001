package com.ssms.common.config;

import com.ssms.common.aop.SentryClientAspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.ssms.common.error.GlobalExceptionTranslator;
import com.ssms.common.aop.*;

/**
 * Use this common config for Rest API
 */
@Configuration
@Import(value = {SSMSConfig.class, SentryClientAspect.class, GlobalExceptionTranslator.class})
public class SSMSRestConfig  {
}
