package com.ssms.company.controller;

import com.ssms.company.dto.*;
import com.ssms.company.model.Company;
import com.ssms.company.model.Team;
import com.ssms.company.repo.CompanyRepo;
import com.ssms.company.repo.TeamRepo;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import com.ssms.account.client.AccountClient;
import com.ssms.account.dto.TrackEventRequest;
import com.ssms.common.api.BaseResponse;
import com.ssms.common.auth.AuthConstant;
import com.ssms.company.TestConfig;
import com.ssms.company.client.CompanyClient;
import com.ssms.company.dto.*;
import com.ssms.company.repo.JobRepo;

import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@DirtiesContext // avoid port conflict
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(basePackages = {"com.ssms.company.client"})
@Import(TestConfig.class)
@Slf4j
public class JobControllerTest {
    @Autowired
    JobRepo jobRepo;

    @Autowired
    CompanyClient companyClient;

    @MockBean
    AccountClient accountClient;

    @MockBean
    CompanyRepo companyRepo;

    @MockBean
    TeamRepo teamRepo;

    @Before
    public void setUp() {
        // cleanup
        jobRepo.deleteAll();
    }

    @Test
    public void testJob() {
        // arrange mock
        when(accountClient.trackEvent(any(TrackEventRequest.class))).thenReturn(BaseResponse.builder().build());

        String companyId = UUID.randomUUID().toString();
        Company company = Company.builder()
                .name("test_company001")
                .id(companyId)
                .defaultDayWeekStarts("Sunday")
                .defaultTimezone(TimeZone.getDefault().getID())
                .build();
        when(companyRepo.findCompanyById(companyId)).thenReturn(company);

        Team team = Team.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .name("test_team001")
                .color("#48B7AB")
                .dayWeekStarts("Monday")
                .timezone(TimeZone.getDefault().getID())
                .build();
        when(teamRepo.findById(team.getId())).thenReturn(Optional.of(team));

        // create job1
        CreateJobRequest createJobRequest1 = CreateJobRequest.builder()
                .name("testjob1")
                .companyId(companyId)
                .teamId(team.getId())
                .color("#48B7AB")
                .build();
        GenericJobResponse genericJobResponse =
                companyClient.createJob(AuthConstant.AUTHORIZATION_SUPPORT_USER, createJobRequest1);
        log.info(genericJobResponse.toString());
        assertThat(genericJobResponse.isSuccess()).isTrue();
        JobDto jobDto1 = genericJobResponse.getJob();
        assertThat(jobDto1.getName()).isEqualTo(createJobRequest1.getName());
        assertThat(jobDto1.getCompanyId()).isEqualTo(createJobRequest1.getCompanyId());
        assertThat(jobDto1.getTeamId()).isEqualTo(createJobRequest1.getTeamId());
        assertThat(jobDto1.getColor()).isEqualTo(createJobRequest1.getColor());

        // create job2
        CreateJobRequest createJobRequest2 = CreateJobRequest.builder()
                .name("testjob2")
                .companyId(companyId)
                .teamId(team.getId())
                .color("#48B7CC")
                .build();
        genericJobResponse =
                companyClient.createJob(AuthConstant.AUTHORIZATION_SUPPORT_USER, createJobRequest2);
        log.info(genericJobResponse.toString());
        assertThat(genericJobResponse.isSuccess()).isTrue();
        JobDto jobDto2 = genericJobResponse.getJob();
        assertThat(jobDto2.getName()).isEqualTo(createJobRequest2.getName());
        assertThat(jobDto2.getCompanyId()).isEqualTo(createJobRequest2.getCompanyId());
        assertThat(jobDto2.getTeamId()).isEqualTo(createJobRequest2.getTeamId());
        assertThat(jobDto2.getColor()).isEqualTo(createJobRequest2.getColor());

        // update job
        JobDto jobDtoToUpdate = jobDto2;
        jobDtoToUpdate.setName("testjob2_update");
        jobDtoToUpdate.setColor("#48B7AB");
        genericJobResponse = companyClient.updateJob(AuthConstant.AUTHORIZATION_SUPPORT_USER, jobDtoToUpdate);
        assertThat(genericJobResponse.isSuccess()).isTrue();
        JobDto updatedJobDto2 = genericJobResponse.getJob();
        assertThat(updatedJobDto2).isEqualTo(jobDtoToUpdate);

        // list job
        ListJobResponse listJobResponse =
                companyClient.listJobs(AuthConstant.AUTHORIZATION_SUPPORT_USER, companyId, team.getId());
        log.info(listJobResponse.toString());
        assertThat(listJobResponse.isSuccess()).isTrue();
        JobList jobList = listJobResponse.getJobList();
        assertThat(jobList.getJobs()).containsExactly(jobDto1, updatedJobDto2);

        // get job
        genericJobResponse =
                companyClient.getJob(AuthConstant.AUTHORIZATION_BOT_SERVICE, jobDto1.getId(), companyId, team.getId());
        log.info(genericJobResponse.toString());
        assertThat(genericJobResponse.isSuccess()).isTrue();
        JobDto jobDto3 = genericJobResponse.getJob();
        assertThat(jobDto3).isEqualTo(jobDto1);
    }

    @After
    public void destroy() {
        // cleanup
        jobRepo.deleteAll();
    }

}
