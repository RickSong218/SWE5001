package com.ssms.common.config;

import com.github.structlog4j.StructLog4J;
import com.github.structlog4j.json.JsonFormatter;
import feign.RequestInterceptor;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.ssms.common.auth.AuthorizeInterceptor;
import com.ssms.common.auth.FeignRequestHeaderInterceptor;
import com.ssms.common.env.EnvConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
@EnableConfigurationProperties(com.ssms.common.config.ssmsProps.class)
public class ssmsConfig implements WebMvcConfigurer {

    @Value("${spring.profiles.active:NA}")
    private String activeProfile;

    @Value("${spring.application.name:NA}")
    private String appName;

    @Autowired
    com.ssms.common.config.ssmsProps ssmsProps;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public EnvConfig envConfig() {
        return EnvConfig.getEnvConfg(activeProfile);
    }

    @Bean
    public SentryClient sentryClient() {

        SentryClient sentryClient = Sentry.init(ssmsProps.getSentryDsn());
        sentryClient.setEnvironment(activeProfile);
        sentryClient.setRelease(ssmsProps.getDeployEnv());
        sentryClient.addTag("service", appName);

        return sentryClient;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthorizeInterceptor());
    }

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new FeignRequestHeaderInterceptor();
    }

    @PostConstruct
    public void init() {
        // init structured logging
        StructLog4J.setFormatter(JsonFormatter.getInstance());

        // global log fields setting
        StructLog4J.setMandatoryContextSupplier(() -> new Object[]{
                "env", activeProfile,
                "service", appName});
    }

    @PreDestroy
    public void destroy() {
        sentryClient().closeConnection();
    }
}
