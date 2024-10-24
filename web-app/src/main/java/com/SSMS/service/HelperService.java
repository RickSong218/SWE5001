package com.SSMS.service;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.SSMS.account.client.AccountClient;
import com.SSMS.account.dto.AccountDto;
import com.SSMS.account.dto.SyncUserRequest;
import com.SSMS.account.dto.TrackEventRequest;
import com.SSMS.common.api.BaseResponse;
import com.SSMS.common.error.ServiceException;
import com.SSMS.company.dto.CompanyDto;
import com.SSMS.mail.client.MailClient;
import com.SSMS.mail.dto.EmailRequest;
import com.SSMS.config.AppConfig;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class HelperService {

    static final ILogger logger = SLoggerFactory.getLogger(HelperService.class);

    static final String METHOD_POST = "POST";

    @Autowired
    AccountClient accountClient;

    @Autowired
    SentryClient sentryClient;

    @Autowired
    MailClient mailClient;

    public static boolean isPost(HttpServletRequest request) {
        return METHOD_POST.equals(request.getMethod());
    }

    public void logError(ILogger log, String errMsg) {
        log.error(errMsg);
        sentryClient.sendMessage(errMsg);
    }

    public void logException(ILogger log, Exception ex, String errMsg) {
        log.error(errMsg, ex);
        sentryClient.sendException(ex);
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void trackEventAsync(String userId, String event) {
        TrackEventRequest trackEventRequest = TrackEventRequest.builder()
                .userId(userId).event(event).build();
        BaseResponse baseResponse = null;
        try {
            baseResponse = accountClient.trackEvent(trackEventRequest);
        } catch (Exception ex) {
            String errMsg = "fail to trackEvent through accountClient";
            logException(logger, ex, errMsg);
        }
        if (!baseResponse.isSuccess()) {
            logError(logger, baseResponse.getMessage());
        }
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void syncUserAsync(String userId) {
        SyncUserRequest request = SyncUserRequest.builder().userId(userId).build();
        accountClient.syncUser(request);
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void sendEmailAsync(AccountDto a, CompanyDto c) {
        EmailRequest emailRequest = EmailRequest.builder()
                .to("sales@staffjoy.xyz")
                .name("")
                .subject(String.format("%s from %s just joined Staffjoy", a.getName(), c.getName()))
                .htmlBody(String.format("Name: %s<br>Phone: %s<br>Email: %s<br>Company: %s<br>App: https://app.staffjoy.com/#/companies/%s/employees/",
                        a.getName(),
                        a.getPhoneNumber(),
                        a.getEmail(),
                        c.getName(),
                        c.getId()))
                .build();

        BaseResponse baseResponse = null;
        try {
            baseResponse = mailClient.send(emailRequest);
        } catch (Exception ex) {
            String errMsg = "Unable to send email";
            logException(logger, ex, errMsg);
        }
        if (!baseResponse.isSuccess()) {
            logError(logger, baseResponse.getMessage());
        }
    }

    public static String buildUrl(String scheme, String host) {
        return buildUrl(scheme, host, null);
    }

    public static String buildUrl(String scheme, String host, String path) {
        try {
            URI uri = new URI(scheme, host, path, null);
            return uri.toString();
        } catch (URISyntaxException ex) {
            String errMsg = "Internal uri parsing exception.";
            logger.error(errMsg);
            throw new ServiceException(errMsg, ex);
        }
    }
}
