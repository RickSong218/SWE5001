package com.ssms.company.controller;

import com.ssms.company.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ssms.common.api.BaseResponse;
import com.ssms.common.auth.AuthConstant;
import com.ssms.common.auth.AuthContext;
import com.ssms.common.auth.Authorize;
import com.ssms.company.dto.*;
import com.ssms.company.service.PermissionService;
import com.ssms.company.service.ShiftService;

@RestController
@RequestMapping("/v1/company/shift")
@Validated
public class ShiftController {
    @Autowired
    ShiftService shiftService;

    @Autowired
    PermissionService permissionService;

    @PostMapping(path = "/create")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericShiftResponse createShift(@RequestBody @Validated CreateShiftRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }

        ShiftDto shiftDto = this.shiftService.createShift(request);

        return new GenericShiftResponse(shiftDto);
    }
    // 调用ShiftService的createShift方法创建新的Shift
    @PostMapping(path = "/list_worker_shifts")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_BOT_SERVICE,
            AuthConstant.AUTHORIZATION_ICAL_SERVICE
    })
    public GenericShiftListResponse listWorkerShifts(@RequestBody @Validated WorkerShiftListRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            // TODO need confirm
            permissionService.checkPermissionTeamWorker(request.getCompanyId(), request.getTeamId());
        }
        // 获取指定工人的班次列表
        ShiftList shiftList = shiftService.listWorkerShifts(request);
        // 返回包含班次列表的响应
        return new GenericShiftListResponse(shiftList);
    }

    @PostMapping(path = "/list_shifts")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericShiftListResponse listShifts(@RequestBody @Validated ShiftListRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionTeamWorker(request.getCompanyId(), request.getTeamId());
        }
        // 获取班次列表
        ShiftList shiftList = shiftService.listShifts(request);
        // 返回包含班次列表的响应
        return new GenericShiftListResponse(shiftList);
    }

    @PostMapping(path = "/bulk_publish")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericShiftListResponse bulkPublishShifts(@RequestBody @Validated BulkPublishShiftsRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionTeamWorker(request.getCompanyId(), request.getTeamId());
        }
        // 批量发布班次
        ShiftList shiftList = shiftService.bulkPublishShifts(request);
        // 返回包含批量发布后的班次列表的响应
        return new GenericShiftListResponse(shiftList);
    }

    @GetMapping(path = "/get")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericShiftResponse getShift(@RequestParam String shiftId, @RequestParam String teamId, @RequestParam  String companyId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionTeamWorker(companyId, teamId);
        }
        // 获取指定的班次详细信息
        ShiftDto shiftDto = shiftService.getShift(shiftId, teamId, companyId);
        // 返回包含班次详细信息的响应
        return new GenericShiftResponse(shiftDto);
    }

    @PutMapping(path = "/update")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericShiftResponse updateShift(@RequestBody @Validated ShiftDto shiftDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionCompanyAdmin(shiftDto.getCompanyId());
        }
        // 更新班次
        ShiftDto updatedShiftDto = shiftService.updateShift(shiftDto);
        // 返回包含更新后班次的响应
        return new GenericShiftResponse(updatedShiftDto);
    }

    @DeleteMapping(path = "/delete")
    @Authorize(value = {
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse deleteShift(@RequestParam String shiftId, @RequestParam String teamId, @RequestParam String companyId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            permissionService.checkPermissionTeamWorker(companyId, teamId);
        }
        // 删除指定的班次
        shiftService.deleteShift(shiftId, teamId, companyId);
        // 返回成功删除的响应
        return BaseResponse.builder().message("shift deleted").build();
    }
}
