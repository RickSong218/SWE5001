package com.ssms.controller;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.ssms.account.client.AccountClient;
import com.ssms.account.dto.AccountDto;
import com.ssms.account.dto.GenericAccountResponse;
import com.ssms.account.dto.CreateAccountRequest;
import com.ssms.common.auth.AuthConstant;
import com.ssms.service.HelperService;
import com.ssms.view.Constant;
import com.ssms.view.PageFactory;

@Controller
public class SignupController {

    static final String SIGN_UP_REDIRECT_PATH = "redirect:/sign-up";

    static final ILogger logger = SLoggerFactory.getLogger(LoginController.class);

    @Autowired
    private PageFactory pageFactory;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private HelperService helperService;

    @PostMapping(value="/confirm")
    public String signUp(@RequestParam(value = "name", required = false) String name, @RequestParam("email") String email, Model model) {
        if (!StringUtils.hasText(email)) {
            return SIGN_UP_REDIRECT_PATH;
        }

        CreateAccountRequest request = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .build();

        GenericAccountResponse genericAccountResponse = null;
        try {
            genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, request);
        } catch (Exception ex) {
            String errMsg = "Failed to create account";
            helperService.logException(logger, ex, errMsg);
            return SIGN_UP_REDIRECT_PATH;
        }

        if (!genericAccountResponse.isSuccess()) {
            helperService.logError(logger, genericAccountResponse.getMessage());
            return SIGN_UP_REDIRECT_PATH;
        }
        AccountDto account = genericAccountResponse.getAccount();
        logger.info(String.format("New Account signup - %s", account));

        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, pageFactory.buildConfirmPage());
        return Constant.VIEW_CONFIRM;
    }
}
