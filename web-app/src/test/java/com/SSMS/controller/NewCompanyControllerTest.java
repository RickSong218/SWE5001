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
import com.SSMS.account.dto.SyncUserRequest;
import com.SSMS.account.dto.TrackEventRequest;
import com.SSMS.common.api.BaseResponse;
import com.SSMS.common.auth.AuthConstant;
import com.SSMS.common.env.EnvConfig;
import com.SSMS.company.client.CompanyClient;
import com.SSMS.company.dto.*;
import com.SSMS.service.HelperService;
import com.SSMS.view.Constant;
import com.SSMS.view.PageFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class NewCompanyControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccountClient accountClient;

    @MockBean
    CompanyClient companyClient;

    @Autowired
    EnvConfig envConfig;

    @Autowired
    PageFactory pageFactory;

    @Test
    public void testNotLoggedin() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/new_company")
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_ANONYMOUS_WEB))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"))
                .andReturn();

        mvcResult = mockMvc.perform(post("/new_company"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"))
                .andReturn();
    }

    @Test
    public void testGetNewCompanyPage() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/new_company")
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_AUTHENTICATED_USER))
                .andExpect(status().isOk())
                .andExpect(view().name(Constant.VIEW_NEW_COMPANY))
                .andExpect(content().string(containsString(pageFactory.buildNewCompanyPage().getDescription())))
                .andReturn();
    }

    @Test
    public void testCreateNewCompany() throws Exception {
        String name = "test_user";
        String email = "test@staffjoy.xyz";
        Instant memberSince = Instant.now().minus(100, ChronoUnit.DAYS);
        String userId = UUID.randomUUID().toString();
        String companyId = UUID.randomUUID().toString();

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

        CompanyDto companyDto = CompanyDto.builder()
                .name("test_company")
                .id(companyId)
                .defaultDayWeekStarts("Monday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();

        when(companyClient.createCompany(anyString(), any(CompanyDto.class)))
                .thenReturn(new GenericCompanyResponse(companyDto));

        when(companyClient.createDirectory(anyString(), any(NewDirectoryEntry.class)))
                .thenReturn(new GenericDirectoryResponse());

        when(companyClient.createAdmin(anyString(), any(DirectoryEntryRequest.class)))
                .thenReturn(new GenericDirectoryResponse());

        TeamDto teamDto1 = TeamDto.builder()
                .companyId(companyId)
                .name("test_team1")
                .dayWeekStarts("Monday")
                .color("#3D85C6")
                .timezone(TimeZone.getDefault().getID())
                .build();
        when(companyClient.createTeam(anyString(), any(CreateTeamRequest.class)))
                .thenReturn(new GenericTeamResponse(teamDto1));

        when(companyClient.createWorker(anyString(), any(WorkerDto.class)))
                .thenReturn(new GenericDirectoryResponse());

        when(accountClient.trackEvent(any(TrackEventRequest.class)))
                .thenReturn(BaseResponse.builder().message("event tracked").build());
        when(accountClient.syncUser(any(SyncUserRequest.class)))
                .thenReturn(BaseResponse.builder().message("user synced").build());

        MvcResult mvcResult = mockMvc.perform(post("/new_company")
                .header(AuthConstant.AUTHORIZATION_HEADER, AuthConstant.AUTHORIZATION_AUTHENTICATED_USER)
                .header(AuthConstant.CURRENT_USER_HEADER, userId)
                .param("name", companyDto.getName()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" +
                        HelperService.buildUrl("http", "app." + envConfig.getExternalApex())))
                .andReturn();
    }
}
