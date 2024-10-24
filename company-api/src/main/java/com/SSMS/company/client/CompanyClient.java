package com.ssms.company.client;

import com.ssms.common.api.BaseResponse;
import com.ssms.common.auth.AuthConstant;
import com.ssms.common.validation.Group1;
import com.ssms.common.validation.Group2;
import com.ssms.company.CompanyConstant;
import com.ssms.company.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = CompanyConstant.SERVICE_NAME, path = "/v1/company", url = "${staffjoy.company-service-endpoint}")
public interface CompanyClient {
    // Company Apis
    @PostMapping(path = "/create")
    com.ssms.company.dto.GenericCompanyResponse createCompany(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated({Group2.class}) com.SSMS.company.dto.CompanyDto companyDto);

    @GetMapping(path = "/list")
    com.ssms.company.dto.ListCompanyResponse listCompanies(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam int offset, @RequestParam int limit);

    @GetMapping(path= "/get")
    com.ssms.company.dto.GenericCompanyResponse getCompany(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam("company_id") String companyId);

    @PutMapping(path= "/update")
    com.ssms.company.dto.GenericCompanyResponse updateCompany(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated({Group1.class}) com.SSMS.company.dto.CompanyDto companyDto);

    // Admin Apis
    @GetMapping(path = "/admin/list")
    com.ssms.company.dto.ListAdminResponse listAdmins(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId);

    @GetMapping(path = "/admin/get")
    com.ssms.company.dto.GenericDirectoryResponse getAdmin(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam String userId);

    @PostMapping(path = "/admin/create")
    com.ssms.company.dto.GenericDirectoryResponse createAdmin(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated com.SSMS.company.dto.DirectoryEntryRequest request);

    @DeleteMapping(path = "/admin/delete")
    BaseResponse deleteAdmin(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated com.SSMS.company.dto.DirectoryEntryRequest request);

    @GetMapping(path = "/admin/admin_of")
    com.ssms.company.dto.GetAdminOfResponse getAdminOf(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String userId);

    // Directory Apis
    @PostMapping(path = "/directory/create")
    com.ssms.company.dto.GenericDirectoryResponse createDirectory(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated NewDirectoryEntry request);

    @GetMapping(path = "/directory/list")
    com.ssms.company.dto.ListDirectoryResponse listDirectories(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam int offset, @RequestParam int limit);

    @GetMapping(path = "/directory/get")
    com.ssms.company.dto.GenericDirectoryResponse getDirectoryEntry(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam String userId);

    @PutMapping(path = "/directory/update")
    com.ssms.company.dto.GenericDirectoryResponse updateDirectoryEntry(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated com.SSMS.company.dto.DirectoryEntryDto request);

    @GetMapping(path = "/directory/get_associations")
    GetAssociationResponse getAssociations(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam int offset, @RequestParam int limit);

    // WorkerDto Apis
    @GetMapping(path = "/worker/list")
    ListWorkerResponse listWorkers(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam String teamId);

    @GetMapping(path = "/worker/get")
    com.ssms.company.dto.GenericDirectoryResponse getWorker(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam String teamId, @RequestParam String userId);

    @DeleteMapping(path = "/worker/delete")
    BaseResponse deleteWorker(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated WorkerDto workerDto);

    @GetMapping(path = "/worker/get_worker_of")
    GetWorkerOfResponse getWorkerOf(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String userId);

    @PostMapping(path = "/worker/create")
    com.ssms.company.dto.GenericDirectoryResponse createWorker(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated WorkerDto workerDto);

    // Team Apis
    @PostMapping(path = "/team/create")
    com.ssms.company.dto.GenericTeamResponse createTeam(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated com.SSMS.company.dto.CreateTeamRequest request);

    @GetMapping(path = "/team/list")
    com.ssms.company.dto.ListTeamResponse listTeams(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId);

    @GetMapping(path = "/team/get")
    com.ssms.company.dto.GenericTeamResponse getTeam(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam String teamId);

    @PutMapping(path = "/team/update")
    com.ssms.company.dto.GenericTeamResponse updateTeam(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated com.SSMS.company.dto.TeamDto teamDto);

    @GetMapping(path = "/team/get_worker_team_info")
    com.ssms.company.dto.GenericWorkerResponse getWorkerTeamInfo(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam(required = false) String companyId, @RequestParam String userId);

    // Job Apis
    @PostMapping(path = "/job/create")
    GenericJobResponse createJob(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated CreateJobRequest request);

    @GetMapping(path = "/job/list")
    ListJobResponse listJobs(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String companyId, @RequestParam String teamId);

    @GetMapping(path = "/job/get")
    GenericJobResponse getJob(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String jobId, @RequestParam String companyId, @RequestParam String teamId);

    @PutMapping(path = "/job/update")
    GenericJobResponse updateJob(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated JobDto jobDto);

    // Shift Apis
    @PostMapping(path = "/shift/create")
    GenericShiftResponse createShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated CreateShiftRequest request);

    @PostMapping(path = "/shift/list_worker_shifts")
    GenericShiftListResponse listWorkerShifts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated WorkerShiftListRequest request);

    @PostMapping(path = "/shift/list_shifts")
    GenericShiftListResponse listShifts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated ShiftListRequest request);

    @PostMapping(path = "/shift/bulk_publish")
    GenericShiftListResponse bulkPublishShifts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated BulkPublishShiftsRequest request);

    @GetMapping(path = "/shift/get")
    GenericShiftResponse getShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String shiftId, @RequestParam String teamId, @RequestParam String companyId);

    @PutMapping(path = "/shift/update")
    GenericShiftResponse updateShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Validated ShiftDto shiftDto);

    @DeleteMapping(path = "/shift/delete")
    BaseResponse deleteShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String shiftId, @RequestParam String teamId, @RequestParam String companyId);
}
