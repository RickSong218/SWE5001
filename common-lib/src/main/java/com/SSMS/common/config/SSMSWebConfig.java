package com.SSMS.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.SSMS.common.aop.SentryClientAspect;

/**
 * Use this common config for Web App
 */
@Configuration
@Import(value = {SSMSConfig.class, SentryClientAspect.class,})
public class SSMSWebConfig {
}
