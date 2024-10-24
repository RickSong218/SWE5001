package com.ssms.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.ssms.common.aop.SentryClientAspect;

/**
 * Use this common config for Web App
 */
@Configuration
@Import(value = {com.ssms.common.config.ssmsConfig.class, SentryClientAspect.class,})
public class ssmsWebConfig {
}
