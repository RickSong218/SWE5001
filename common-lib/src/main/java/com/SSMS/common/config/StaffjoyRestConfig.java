package com.SSMS.common.config;

import com.SSMS.common.aop.SentryClientAspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.SSMS.common.error.GlobalExceptionTranslator;
import xyz.staffjoy.common.aop.*;

/**
 * Use this common config for Rest API
 */
@Configuration
@Import(value = {StaffjoyConfig.class, SentryClientAspect.class, GlobalExceptionTranslator.class})
public class StaffjoyRestConfig  {
}
