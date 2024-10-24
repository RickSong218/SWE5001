package com.ssms.whoami.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.junit4.SpringRunner;
import com.ssms.account.client.AccountClient;
import com.ssms.account.dto.AccountDto;
import com.ssms.account.dto.GenericAccountResponse;
import com.ssms.common.auth.AuthConstant;
import com.ssms.common.crypto.Hash;
import com.ssms.company.client.CompanyClient;
import com.ssms.company.dto.*;
import com.ssms.whoami.TestConfig;
import com.ssms.whoami.client.WhoAmIClient;
import com.ssms.whoami.dto.FindWhoAmIResponse;
import com.ssms.whoami.dto.GetIntercomSettingResponse;
import com.ssms.whoami.dto.IAmDto;
import com.ssms.whoami.dto.IntercomSettingsDto;
import com.ssms.whoami.props.AppProps;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@DirtiesContext // avoid port conflict
@EnableFeignClients(basePackages = {"com.ssms.whoami.client"})
@Slf4j
public class WhoAmIControllerTest {
    @Autowired
    WhoAmIClient whoAmIClient;

    @MockBean
    CompanyClient companyClient;

    @MockBean
    AccountClient accountClient;

    @Autowired
    AppProps appProps;

    @Before
    public void setUp() {
        TestConfig.TEST_USER_ID = UUID.randomUUID().toString();
    }

    @Test
    public void testFindWhoAmI() {
        // arrange mocks
        String companyId = UUID.randomUUID().toString();

        TeamDto teamDto1 = TeamDto.builder()
                .companyId(companyId)
                .name("test_team1")
                .dayWeekStarts("Monday")
                .color("#3D85C6")
                .timezone(TimeZone.getDefault().getID())
                .build();
        TeamDto teamDto2 = TeamDto.builder()
                .companyId(companyId)
                .name("test_team2")
                .dayWeekStarts("Monday")
                .color("#95AF44")
                .timezone(TimeZone.getDefault().getID())
                .build();

        WorkerOfList workerOfList = WorkerOfList.builder()
                .userId(TestConfig.TEST_USER_ID)
                .build();
        workerOfList.getTeams().add(teamDto1);
        workerOfList.getTeams().add(teamDto2);

        when(companyClient.getWorkerOf(AuthConstant.AUTHORIZATION_WHOAMI_SERVICE, TestConfig.TEST_USER_ID))
                .thenReturn(new GetWorkerOfResponse(workerOfList));

        CompanyDto companyDto = CompanyDto.builder()
                .name("test_company")
                .id(companyId)
                .defaultDayWeekStarts("Monday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();
        AdminOfList adminOfList = AdminOfList.builder()
                .userId(TestConfig.TEST_USER_ID)
                .build();
        adminOfList.getCompanies().add(companyDto);
        when(companyClient.getAdminOf(AuthConstant.AUTHORIZATION_WHOAMI_SERVICE, TestConfig.TEST_USER_ID)).
                thenReturn(new GetAdminOfResponse(adminOfList));

        FindWhoAmIResponse findWhoAmIResponse = whoAmIClient.findWhoAmI(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER);
        log.info(findWhoAmIResponse.toString());
        assertThat(findWhoAmIResponse.isSuccess()).isTrue();
        IAmDto iAmDto = findWhoAmIResponse.getIAm();
        assertThat(iAmDto.getWorkerOfList().getUserId()).isEqualTo(TestConfig.TEST_USER_ID);
        assertThat(iAmDto.getWorkerOfList().getTeams()).containsExactly(teamDto1, teamDto2);
        assertThat(iAmDto.getAdminOfList().getUserId()).isEqualTo(TestConfig.TEST_USER_ID);
        assertThat(iAmDto.getAdminOfList().getCompanies()).containsExactly(companyDto);
    }

    @Test
    public void testGetIntercomSettings() throws Exception {
        String name = "test_user";
        String email = "test@staffjoy.xyz";
        Instant memberSince = Instant.now().minus(100, ChronoUnit.DAYS);
        AccountDto accountDto = AccountDto.builder()
                .id(TestConfig.TEST_USER_ID)
                .name(name)
                .email(email)
                .memberSince(memberSince)
                .phoneNumber("18001112222")
                .confirmedAndActive(true)
                .photoUrl("http://www.staffjoy.xyz/photo/test_user.png")
                .build();

        when(accountClient.getAccount(AuthConstant.AUTHORIZATION_WHOAMI_SERVICE, TestConfig.TEST_USER_ID))
                .thenReturn(new GenericAccountResponse(accountDto));

        GetIntercomSettingResponse getIntercomSettingResponse =
                whoAmIClient.getIntercomSettings(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER);
        log.info(getIntercomSettingResponse.toString());
        assertThat(getIntercomSettingResponse.isSuccess()).isTrue();
        IntercomSettingsDto intercomSettingsDto = getIntercomSettingResponse.getIntercomSettings();

        assertThat(intercomSettingsDto.getAppId()).isEqualTo(appProps.getIntercomAppId());
        assertThat(intercomSettingsDto.getName()).isEqualTo(name);
        assertThat(intercomSettingsDto.getEmail()).isEqualTo(email);
        assertThat(intercomSettingsDto.getUserId()).isEqualTo(TestConfig.TEST_USER_ID);
        assertThat(intercomSettingsDto.getCreatedAt()).isEqualTo(memberSince);
        assertThat(intercomSettingsDto.getUserHash())
                .isEqualTo(Hash.encode(appProps.getIntercomSigningSecret(), TestConfig.TEST_USER_ID));
    }
}
