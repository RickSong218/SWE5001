package com.ssms.company.controller;

import com.ssms.company.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ssms.common.auth.AuthConstant;
import com.ssms.common.auth.AuthContext;
import com.ssms.common.auth.Authorize;
import com.ssms.company.dto.*;
import com.ssms.company.service.JobService;
import com.ssms.company.service.PermissionService;

@RestController
@RequestMapping("/v1/company/job")
@Validated
public class JobController {
    @Autowired
    JobService jobService;

    @Autowired
    PermissionService permissionService;

    // 创建Job的API
    @PostMapping(path = "/create")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,  // 授权已认证用户
            AuthConstant.AUTHORIZATION_SUPPORT_USER         // 授权支持用户
    })
    public GenericJobResponse createJob(@RequestBody @Validated CreateJobRequest request) {
        // 如果当前用户是已认证用户，检查是否有公司管理员的权限
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }
        // 调用JobService的createJob方法创建新的Job
        JobDto jobDto = jobService.createJob(request);
        // 返回包含新创建Job的响应
        return new GenericJobResponse(jobDto);
    }

    @GetMapping(path = "/list")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public ListJobResponse listJobs(@RequestParam String companyId, @RequestParam String teamId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) { // TODO need confirm
            permissionService.checkPermissionTeamWorker(companyId, teamId);
        }
        // 调用JobService的listJobs方法，获取指定公司和团队的Job列表
        JobList jobList = jobService.listJobs(companyId, teamId);

        return new ListJobResponse(jobList);
    }

    @GetMapping(path = "/get")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_BOT_SERVICE
    })
    public GenericJobResponse getJob(String jobId, String companyId, String teamId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionTeamWorker(companyId, teamId);
        }
        // 调用JobService的getJob方法，获取指定Job的详细信息
        JobDto jobDto = jobService.getJob(jobId, companyId, teamId);

        return new GenericJobResponse(jobDto);
    }

    @PutMapping(path = "/update")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericJobResponse updateJob(@RequestBody @Validated JobDto jobDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(jobDto.getCompanyId());
        }
        // 调用JobService的updateJob方法，更新指定的Job
        JobDto updatedJobDto = jobService.updateJob(jobDto);

        return new GenericJobResponse(updatedJobDto);
    }
}
