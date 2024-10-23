package com.SSMS.company.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.SSMS.common.api.BaseResponse;
import com.SSMS.common.auth.AuthConstant;
import com.SSMS.common.auth.AuthContext;
import com.SSMS.common.auth.Authorize;
import com.SSMS.company.dto.*;
import com.SSMS.company.service.PermissionService;
import com.SSMS.company.service.WorkerService;

@RestController
@RequestMapping("/v1/company/worker")
@Validated
public class WorkerController {
    @Autowired
    WorkerService workerService;

    @Autowired
    PermissionService permissionService;

    // 列出团队的工人列表
    @GetMapping(path = "/list")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public ListWorkerResponse listWorkers(@RequestParam String companyId, @RequestParam String teamId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionTeamWorker(companyId, teamId);
        }
        WorkerEntries workerEntries = workerService.listWorkers(companyId, teamId);
        return new ListWorkerResponse(workerEntries);
    }
    // 获取单个工人的详细信息
    @GetMapping(path = "/get")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WWW_SERVICE
    })
    public GenericDirectoryResponse getWorker(@RequestParam  String companyId, @RequestParam String teamId, @RequestParam String userId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionTeamWorker(companyId, teamId);
        }
        DirectoryEntryDto directoryEntryDto = workerService.getWorker(companyId, teamId, userId);
        return new GenericDirectoryResponse(directoryEntryDto);
    }
    // 删除工人的API
    @DeleteMapping(path = "/delete")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse deleteWorker(@RequestBody @Validated WorkerDto workerDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(workerDto.getCompanyId());
        }
        workerService.deleteWorker(workerDto.getCompanyId(), workerDto.getTeamId(), workerDto.getUserId());
        return BaseResponse.builder().message("worker has been deleted").build();
    }
    // 获取某用户的所有工人记录
    @GetMapping(path = "/get_worker_of")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE,
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            // This is an internal endpoint
            AuthConstant.AUTHORIZATION_WHOAMI_SERVICE
    })
    public GetWorkerOfResponse getWorkerOf(@RequestParam String userId) {
        WorkerOfList workerOfList = workerService.getWorkerOf(userId);
        return new GetWorkerOfResponse(workerOfList);
    }
    // 创建新的工人的API
    @PostMapping(path = "/create")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_WHOAMI_SERVICE
    })
    public GenericDirectoryResponse createWorker(@RequestBody @Validated WorkerDto workerDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(workerDto.getCompanyId());
        }
        DirectoryEntryDto directoryEntryDto = workerService.createWorker(workerDto);
        return new GenericDirectoryResponse(directoryEntryDto);
    }
}
