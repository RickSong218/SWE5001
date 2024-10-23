package com.SSMS.controller;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.SSMS.account.client.AccountClient;
import com.SSMS.account.dto.AccountDto;
import com.SSMS.account.dto.GenericAccountResponse;
import com.SSMS.account.dto.VerifyPasswordRequest;
import com.SSMS.common.auth.AuthConstant;
import com.SSMS.common.auth.AuthContext;
import com.SSMS.common.auth.Sessions;
import com.SSMS.common.env.EnvConfig;
import com.SSMS.common.env.EnvConstant;
import com.SSMS.common.services.Service;
import com.SSMS.common.services.ServiceDirectory;
import com.SSMS.props.AppProps;
import com.SSMS.service.HelperService;
import com.SSMS.view.Constant;
import com.SSMS.view.LoginPage;
import com.SSMS.view.PageFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@Controller
public class LoginController {

    static final ILogger logger = SLoggerFactory.getLogger(LoginController.class);

    @Autowired
    private PageFactory pageFactory;

    @Autowired
    private EnvConfig envConfig;

    @Autowired
    private AppProps appProps;

    @Autowired
    private HelperService helperService;

    @Autowired
    private AccountClient accountClient;

    @RequestMapping(value = "/login")
    public String login(@RequestParam(value="return_to", required = false) String returnTo, // POST and GET are in the same handler - reset
                        @RequestParam(value="email", required = false) String email,
                        @RequestParam(value="password", required = false) String password,
                        // rememberMe=True means that the session is set for a month instead of a day
                        @RequestParam(value="remember-me", required = false) String rememberMe,
                        Model model,
                        HttpServletRequest request,
                        HttpServletResponse response) {

        LoginPage loginPage = pageFactory.buildLoginPage();
        loginPage.setReturnTo(returnTo); // for GET

        // if logged in - go away
        if (!StringUtils.isEmpty(AuthContext.getAuthz()) && !AuthConstant.AUTHORIZATION_ANONYMOUS_WEB.equals(AuthContext.getAuthz())) {
            String url = HelperService.buildUrl("http", "myaccount." + envConfig.getExternalApex());
            return "redirect:" + url;
        }

        if (HelperService.isPost(request)) {

            AccountDto account = null;
            GenericAccountResponse genericAccountResponse = null;
            try {
                VerifyPasswordRequest verifyPasswordRequest = VerifyPasswordRequest.builder()
                        .email(email)
                        .password(password)
                        .build();
                genericAccountResponse = accountClient.verifyPassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
            } catch (Exception ex) {
                helperService.logException(logger, ex, "fail to verify user password");
            }
            if (genericAccountResponse != null) {
                if (!genericAccountResponse.isSuccess()) {
                    helperService.logError(logger, genericAccountResponse.getMessage());
                } else {
                    account = genericAccountResponse.getAccount();
                }
            }

            if (account != null) { // login success
                // set cookie
                Sessions.loginUser(account.getId(),
                        account.isSupport(),
                        !StringUtils.isEmpty(rememberMe),
                        appProps.getSigningSecret(),
                        envConfig.getExternalApex(),
                        response);
                helperService.trackEventAsync(account.getId(), "login");
                helperService.syncUserAsync(account.getId());

                String scheme  = "https";
                if (envConfig.isDebug()) {
                    scheme = "http";
                }

                if (StringUtils.isEmpty(returnTo)) {
                    returnTo = HelperService.buildUrl(scheme, "app." + envConfig.getExternalApex());
                } else {
                    if (!returnTo.startsWith("http")) {
                        returnTo = "http://" + returnTo;
                    }
                    // sanitize
                    if (!isValidSub(returnTo)) {
                        returnTo = HelperService.buildUrl(scheme, "myaccount." + envConfig.getExternalApex());
                    }
                }

                return "redirect:" + returnTo;

            } else {
                logger.info("Login attempt denied", "email", email);
                loginPage.setDenied(true);
                loginPage.setPreviousEmail(email);
            }

        }

        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, loginPage);
        return Constant.VIEW_LOGIN;
    }

    // isValidSub returns true if url contains valid subdomain
    boolean isValidSub(String sub) {
        URL url = null;
        try {
            url = new URL(sub);
        } catch (MalformedURLException ex) {
            logger.error("can't parse url", ex);
            return false;
        }

        String bare = url.getHost().replaceAll("." + envConfig.getExternalApex(), "");

        Map<String, Service> serviceMap = ServiceDirectory.getMapping();
        for(String key : serviceMap.keySet()) {
            if (key.equals(bare)) {
                return true;
            }
        }

        return false;
    }
}
