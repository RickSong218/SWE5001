package com.ssms.company.service;

import com.ssms.company.dto.*;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.ssms.bot.dto.AlertNewShiftRequest;
import com.ssms.bot.dto.AlertRemovedShiftRequest;
import com.ssms.common.auditlog.LogEntry;
import com.ssms.common.auth.AuthContext;
import com.ssms.company.dto.*;
import com.ssms.company.model.Shift;
import com.ssms.company.repo.ShiftRepo;
import com.ssms.company.service.helper.ServiceHelper;
import com.ssms.company.service.helper.ShiftHelper;

import java.time.Instant;
import java.util.*;

@Service
public class ShiftService {
    // 定义静态日志记录器，用于记录系统日志
    static final ILogger logger = SLoggerFactory.getLogger(ShiftService.class);

    @Autowired
    ShiftRepo shiftRepo;

    @Autowired
    TeamService teamService;

    @Autowired
    JobService jobService;

    @Autowired
    DirectoryService directoryService;

    @Autowired
    ServiceHelper serviceHelper;

    @Autowired
    ShiftHelper shiftHelper;

    @Autowired
    ModelMapper modelMapper; //注入ModelMapper，用于对象之间的映射

    // 创建一个班次（Shift）
    public ShiftDto createShift(CreateShiftRequest req) {
        // 验证团队是否存在，不存在则抛出异常
        teamService.getTeamWithCompanyIdValidation(req.getCompanyId(), req.getTeamId());
        // 如果请求中包含jobId，则验证工作职位是否存在，不存在则抛出异常
        if (!StringUtils.isEmpty(req.getJobId())) {
            jobService.getJob(req.getJobId(), req.getCompanyId(), req.getTeamId());
        }
        // 如果请求中包含userId，则验证用户是否存在
        if (!StringUtils.isEmpty(req.getUserId())) {
            directoryService.getDirectoryEntry(req.getCompanyId(), req.getUserId());
        }
        // 创建Shift对象并填充数据
        Shift shift = Shift.builder()
                .teamId(req.getTeamId())
                .jobId(req.getJobId())
                .start(req.getStart())
                .stop(req.getStop())
                .published(req.isPublished())
                .userId(req.getUserId())
                .build();
        try {
            shiftRepo.save(shift);  // 保存班次信息到数据库
        } catch (Exception ex) {
            String errMsg = "could not create shift";   // 捕获异常并抛出服务异常，记录错误日志
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        // 记录班次创建的审计日志
        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("shift")
                .targetId(shift.getId())
                .companyId(req.getCompanyId())
                .teamId(req.getTeamId())
                .updatedContents(shift.toString())
                .build();
        // 记录日志信息
        logger.info("created shift", auditLog);
        // 将Shift转换为ShiftDto对象并返回
        ShiftDto shiftDto = shiftHelper.convertToDto(shift);
        shiftDto.setCompanyId(req.getCompanyId());
        // 如果用户ID存在并且班次已发布，发送新班次的通知
        if (!StringUtils.isEmpty(shift.getUserId()) && shift.isPublished()) {
            AlertNewShiftRequest alertNewShiftRequest = AlertNewShiftRequest.builder()
                    .userId(shiftDto.getUserId())
                    .newShift(shiftDto)
                    .build();
            serviceHelper.alertNewShiftAsync(alertNewShiftRequest);
        }
        // 异步记录班次创建事件
        serviceHelper.trackEventAsync("shift_created");
        if (req.isPublished()) {
            serviceHelper.trackEventAsync("shift_published");
        }
        // 返回创建的班次信息
        return shiftDto;
    }
    // 列出某个工人的班次列表
    public ShiftList listWorkerShifts(WorkerShiftListRequest req) {
        // 验证团队是否存在
        teamService.getTeamWithCompanyIdValidation(req.getCompanyId(), req.getTeamId());
        // 创建ShiftList对象，设置班次起止时间
        ShiftList shiftList = ShiftList.builder()
                .shiftStartAfter(req.getShiftStartAfter())
                .shiftStartBefore(req.getShiftStartBefore())
                .build();
        // 从数据库中获取指定工人的班次列表
        List<Shift> shifts = shiftRepo.listWorkerShifts(req.getTeamId(), req.getWorkerId(), req.getShiftStartAfter(), req.getShiftStartBefore());
        // 将获取到的班次列表转换为ShiftList对象并返回
        return convertToShiftList(shiftList, shifts, req.getCompanyId());
    }
    // 列出所有班次，根据用户和职位ID进行筛选
    public ShiftList listShifts(ShiftListRequest req) {
        // 验证团队是否存在
        teamService.getTeamWithCompanyIdValidation(req.getCompanyId(), req.getTeamId());
        // 创建ShiftList对象，设置班次起止时间
        ShiftList shiftList = ShiftList.builder()
                .shiftStartAfter(req.getShiftStartAfter())
                .shiftStartBefore(req.getShiftStartBefore())
                .build();

        List<Shift> shifts = null;
        // 根据用户ID和职位ID的组合从数据库中筛选班次(4种排列组合筛选方式)
        // 如果用户ID不为空且职位ID为空，查询该用户的班次
        if (!StringUtils.isEmpty(req.getUserId()) && StringUtils.isEmpty(req.getJobId())) {
            shifts = shiftRepo.listWorkerShifts(req.getTeamId(), req.getUserId(), req.getShiftStartAfter(), req.getShiftStartBefore());
        }
        // 如果职位ID不为空且用户ID为空，查询该职位的班次
        if (!StringUtils.isEmpty(req.getJobId()) && StringUtils.isEmpty(req.getUserId())) {
            shifts = shiftRepo.listShiftByJobId(req.getTeamId(), req.getJobId(), req.getShiftStartAfter(), req.getShiftStartBefore());
        }
        // 如果用户ID和职位ID都不为空，查询该用户和职位的班次
        if (!StringUtils.isEmpty(req.getJobId()) && !StringUtils.isEmpty(req.getUserId())) {
            shifts = shiftRepo.listShiftByUserIdAndJobId(req.getTeamId(), req.getUserId(), req.getJobId(), req.getShiftStartAfter(), req.getShiftStartBefore());
        }
        // 如果用户ID和职位ID都为空，查询该团队的所有班次
        if (StringUtils.isEmpty(req.getJobId()) && StringUtils.isEmpty(req.getUserId())) {
            shifts = shiftRepo.listShiftByTeamIdOnly(req.getTeamId(), req.getShiftStartAfter(), req.getShiftStartBefore());
        }
        // 将筛选的班次列表转换为ShiftList对象并返回
        return convertToShiftList(shiftList, shifts, req.getCompanyId());
    }
    // 将班次列表转换为ShiftList对象
    private ShiftList convertToShiftList(ShiftList shiftList, List<Shift> shifts, String companyId) {
        // 遍历班次列表，将其转换为ShiftDto对象并添加到ShiftList中
        for(Shift shift : shifts) {
            ShiftDto shiftDto = shiftHelper.convertToDto(shift);
            shiftDto.setCompanyId(companyId);
            shiftList.getShifts().add(shiftDto);
        }
        // 返回包含转换后的班次列表的ShiftList对象
        return shiftList;
    }
    // 快速计算时间差
    private long quickTime(long startTime) {
        long endTime = System.currentTimeMillis();
        return (endTime - startTime) / 1000;
    }
    // 批量发布班次
    public ShiftList bulkPublishShifts(BulkPublishShiftsRequest req) {
        long startTime = System.currentTimeMillis();// 记录开始时间
        logger.info(String.format("time so far %d", quickTime(startTime)));// 记录当前耗时
        // 创建ShiftListRequest对象以获取原始班次
        ShiftListRequest shiftListRequest = ShiftListRequest.builder()
                .companyId(req.getCompanyId())
                .teamId(req.getTeamId())
                .userId(req.getUserId())
                .jobId(req.getJobId())
                .shiftStartAfter(req.getShiftStartAfter())
                .shiftStartBefore(req.getShiftStartBefore())
                .build();
        ShiftList orig = this.listShifts(shiftListRequest);// 获取班次列表

        ShiftList shiftList = ShiftList.builder()
                .shiftStartAfter(req.getShiftStartAfter())
                .shiftStartBefore(req.getShiftStartBefore())
                .build();

        // 创建一个通知map，追踪用户和班次的变化
        Map<String, List<ShiftDto>> notifs = new HashMap<>();

        logger.info(String.format("before shifts update %d", quickTime(startTime)));

        for(ShiftDto shiftDto : orig.getShifts()) {
            // 如果班次状态发生变化并且是未来的班次，记录更改
            if (!StringUtils.isEmpty(shiftDto.getUserId()) &&
                    shiftDto.isPublished() != req.isPublished() &&
                    shiftDto.getStart().isAfter(Instant.now())) {
                List<ShiftDto> shiftDtos = notifs.get(shiftDto.getUserId());
                if (shiftDtos == null) {
                    shiftDtos = new ArrayList<>();
                    notifs.put(shiftDto.getUserId(), shiftDtos);
                }
                ShiftDto copy = shiftDto.toBuilder().build();// 创建班次的副本
                shiftDtos.add(copy);
            }
            // 更新班次的发布状态
            shiftDto.setPublished(req.isPublished());

            //shiftHelper.updateShiftAsync(shiftDto);
            shiftHelper.updateShift(shiftDto, true);// 更新班次
            shiftList.getShifts().add(shiftDto);// 添加更新后的班次到列表中
        }
        // 异步构建班次通知
        logger.info(String.format("before shifts notifications %d", quickTime(startTime)));

        serviceHelper.buildShiftNotificationAsync(notifs, req.isPublished());
        logger.info(String.format("total time %d", quickTime(startTime)));// 记录总耗时

        return shiftList;

    }
    // 获取指定班次的详细信息
    public ShiftDto getShift(String shiftId, String teamId, String companyId) {
        return shiftHelper.getShift(shiftId, teamId, companyId);
    }
    // 更新班次信息
    public ShiftDto updateShift(ShiftDto shiftDtoToUpdate) {
        return shiftHelper.updateShift(shiftDtoToUpdate, false);
    }
    // 删除班次
    public void deleteShift(String shiftId, String teamId, String companyId) {
        ShiftDto orig = this.getShift(shiftId, teamId, companyId);

        try {
            // 从数据库中删除班次
            shiftRepo.deleteShiftById(shiftId);
        } catch (Exception ex) {
            // 捕获异常并记录错误日志
            String errMsg = "failed to delete shift";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        // 记录班次删除的审计日志
        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("shift")
                .targetId(shiftId)
                .companyId(companyId)
                .teamId(teamId)
                .originalContents(orig.toString())
                .build();
        // 记录日志
        logger.info("deleted shift", auditLog);
        // 如果用户ID存在，且班次已发布且时间尚未来到，发送班次删除通知
        if (!StringUtils.isEmpty(orig.getUserId()) && orig.isPublished() && orig.getStart().isAfter(Instant.now())) {
            AlertRemovedShiftRequest alertRemovedShiftRequest = AlertRemovedShiftRequest.builder()
                    .userId(orig.getUserId())
                    .oldShift(orig)
                    .build();
            serviceHelper.alertRemovedShiftAsync(alertRemovedShiftRequest);
        }
        // 异步记录班次删除事件
        serviceHelper.trackEventAsync("shift_deleted");
    }

}
