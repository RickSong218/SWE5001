package com.SSMS.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.SSMS.account.client.AccountClient;
import com.SSMS.account.dto.AccountDto;
import com.SSMS.account.dto.GenericAccountResponse;
import com.SSMS.account.dto.UpdatePasswordRequest;
import com.SSMS.common.api.BaseResponse;
import com.SSMS.common.auth.AuthConstant;
import com.SSMS.common.auth.Sessions;
import com.SSMS.common.crypto.Sign;
import com.SSMS.common.env.EnvConfig;
import com.SSMS.company.client.CompanyClient;
import com.SSMS.company.dto.*;
import com.SSMS.props.AppProps;
import com.SSMS.service.HelperService;
import com.SSMS.view.Constant;
import com.SSMS.view.PageFactory;

import javax.servlet.http.Cookie;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ConfirmResetControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccountClient accountClient;
    @MockBean
    CompanyClient companyClient;

    @Autowired
    EnvConfig envConfig;

    @Autowired
    AppProps appProps;

    @Autowired
    PageFactory pageFactory;

    @Test
    public void testGetConfirmReset() throws Exception {

        String userId = UUID.randomUUID().toString();
        String email = "test@staffjoy.xyz";
        String signingToken = appProps.getSigningSecret();
        String token = Sign.generateEmailConfirmationToken(userId, email, signingToken);
        // get request
        MvcResult mvcResult = mockMvc.perform(get("/reset/" + token))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.VIEW_CONFIRM_RESET))
                .andExpect(content().string(containsString(pageFactory.buildConfirmResetPage().getDescription())))
                .andExpect(content().string(containsString(token)))
                .andReturn();
    }

    @Test
    public void testGetConfirmResetWrongToken() throws Exception {

        String userId = UUID.randomUUID().toString();
        String email = "test@staffjoy.xyz";
        String signingToken = appProps.getSigningSecret();
        String token = Sign.generateEmailConfirmationToken(userId, email, signingToken);
        token += "wrong_token";
        // get request
        MvcResult mvcResult = mockMvc.perform(get("/reset/" + token))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" + ResetController.PASSWORD_RESET_PATH))
                .andReturn();
    }

    @Test
    public void testPostConfirmReset() throws Exception {
        String name = "test_user";
        String email = "test@staffjoy.xyz";
        Instant memberSince = Instant.now().minus(100, ChronoUnit.DAYS);
        String userId = UUID.randomUUID().toString();
        String token = Sign.generateEmailConfirmationToken(userId, email, appProps.getSigningSecret());

        AccountDto accountDto = AccountDto.builder()
                .id(userId)
                .name(name)
                .email(email)
                .memberSince(memberSince)
                .phoneNumber("18001112222")
                .confirmedAndActive(true)
                .photoUrl("http://www.staffjoy.xyz/photo/test_user.png")
                .build();

        when(accountClient.getAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GenericAccountResponse(accountDto));
        when(accountClient.updateAccount(anyString(), any(AccountDto.class)))
                .thenReturn(new GenericAccountResponse(accountDto));
        when(accountClient.updatePassword(anyString(), any(UpdatePasswordRequest.class)))
                .thenReturn(BaseResponse.builder().build());
        when(companyClient.getWorkerOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GetWorkerOfResponse(WorkerOfList.builder().build()));
        when(companyClient.getAdminOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GetAdminOfResponse(AdminOfList.builder().build()));

        MvcResult mvcResult = mockMvc.perform(post("/reset/" + token)
                .param("password", "newpassxxx"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" +
                        HelperService.buildUrl("http", "www." + envConfig.getExternalApex(), "/new_company/")))
                .andReturn();
        Cookie cookie = mvcResult.getResponse().getCookie(AuthConstant.COOKIE_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo(AuthConstant.COOKIE_NAME);
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getDomain()).isEqualTo(envConfig.getExternalApex());
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getValue()).isNotBlank();
        assertThat(cookie.getMaxAge()).isEqualTo(Sessions.SHORT_SESSION / 1000);

        // company admin
        AdminOfList adminOfList = AdminOfList.builder().userId(userId).build();
        adminOfList.getCompanies().add(CompanyDto.builder().build());
        when(companyClient.getAdminOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GetAdminOfResponse(adminOfList));
        mvcResult = mockMvc.perform(post("/reset/" + token)
                .param("password", "newpassxxx"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" +
                        HelperService.buildUrl("http", "app." + envConfig.getExternalApex())))
                .andReturn();

        when(companyClient.getAdminOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GetAdminOfResponse(AdminOfList.builder().build()));
        // worker of teams
        WorkerOfList workerOfList = WorkerOfList.builder().userId(userId).build();
        workerOfList.getTeams().add(TeamDto.builder().build());
        when(companyClient.getWorkerOf(AuthConstant.AUTHORIZATION_WWW_SERVICE, userId))
                .thenReturn(new GetWorkerOfResponse(workerOfList));
        mvcResult = mockMvc.perform(post("/reset/" + token)
                .param("password", "newpassxxx"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" +
                        HelperService.buildUrl("http", "myaccount." + envConfig.getExternalApex())))
                .andReturn();
    }
}
