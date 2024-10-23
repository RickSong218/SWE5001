package com.SSMS.company.service;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.SSMS.common.api.ResultCode;
import com.SSMS.common.auditlog.LogEntry;
import com.SSMS.common.auth.AuthContext;
import com.SSMS.common.error.ServiceException;
import com.SSMS.company.dto.*;
import com.SSMS.company.model.Worker;
import com.SSMS.company.repo.WorkerRepo;
import com.SSMS.company.service.helper.ServiceHelper;

import java.util.List;

@Service
public class WorkerService {

    static final ILogger logger = SLoggerFactory.getLogger(WorkerService.class);
    //自动注入
    @Autowired
    WorkerRepo workerRepo;  // 工人的数据访问层

    @Autowired
    TeamService teamService;    // 团队的数据访问层

    @Autowired
    DirectoryService directoryService;  // 目录的数据访问层

    @Autowired
    ServiceHelper serviceHelper;    //帮助类，处理异步操作和错误

    // 列出某个团队的所有工人
    public WorkerEntries listWorkers(String companyId, String teamId) {
        // 验证公司下的团队是否存在，不存在则抛出异常
        teamService.getTeamWithCompanyIdValidation(companyId, teamId);
        // 从数据库中根据团队ID查找所有工人
        List<Worker> workerList = workerRepo.findByTeamId(teamId);
        // 创建WorkerEntries对象，存储团队和公司ID（DTO）
        WorkerEntries workerEntries = WorkerEntries.builder().companyId(companyId).teamId(teamId).build();
        // 遍历工人列表，为每个工人获取其在目录中的信息并添加到workerEntries中
        for(Worker worker : workerList) {
            DirectoryEntryDto directoryEntryDto = directoryService.getDirectoryEntry(companyId, worker.getUserId());
            workerEntries.getWorkers().add(directoryEntryDto);
        }
        // 返回包含所有工人的条目
        return workerEntries;
    }
    // 根据公司ID、团队ID和用户ID获取某个工人的信息
    public DirectoryEntryDto getWorker(String companyId, String teamId, String userId) {
        // 验证团队是否存在，否则抛出异常
        teamService.getTeamWithCompanyIdValidation(companyId, teamId);
        // 从数据库中查找指定团队的工人
        Worker worker = workerRepo.findByTeamIdAndUserId(teamId, userId);
        if (worker == null) {
            // 如果工人不存在，则抛出异常
            throw new ServiceException(ResultCode.NOT_FOUND, "worker relationship not found");
        }
        // 获取工人的目录信息并返回
        DirectoryEntryDto directoryEntryDto = directoryService.getDirectoryEntry(companyId, userId);

        return directoryEntryDto;
    }
    // 删除某个工人
    public void deleteWorker(String companyId, String teamId, String userId) {
        //  调用getWorker方法，确保工人存在，不存在则抛出异常
        this.getWorker(companyId, teamId, userId);

        try {
            workerRepo.deleteWorker(teamId, userId); // 尝试从数据库中删除指定的工人
        } catch (Exception ex) {
            // 如果删除操作失败，记录错误日志并抛出异常
            String errMsg = "failed to delete worker in database";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        // 创建审计日志记录，记录当前用户删除了哪个工人（获取当前用户ID，获取当前用户的授权信息，目标类型为"工人"，记录被删除工人的ID，所属的公司ID，所属的团队ID）
        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("worker")
                .targetId(userId)
                .companyId(companyId)
                .teamId(teamId)
                .build();
        // 记录删除工人的日志
        logger.info("removed worker", auditLog);
        // 异步追踪删除工人的事件
        serviceHelper.trackEventAsync("worker_deleted");
    }

    // 根据用户ID获取该用户作为工人的所有团队
    public WorkerOfList getWorkerOf(String userId) {
        // 从数据库中查找指定用户的工人记录
        List<Worker> workerList = workerRepo.findByUserId(userId);
        // 创建WorkerOfList对象，存储用户ID
        WorkerOfList workerOfList = WorkerOfList.builder().userId(userId).build();
        // 遍历工人列表，为每个工人获取其团队信息并添加到workerOfList中
        for(Worker worker : workerList) {
            TeamDto teamDto = teamService.getTeam(worker.getTeamId());
            workerOfList.getTeams().add(teamDto);
        }
        // 返回用户作为工人的所有团队
        return workerOfList;
    }
    // 创建新的工人记录
    public DirectoryEntryDto createWorker(WorkerDto workerDto) {
        // 验证团队是否存在
        teamService.getTeamWithCompanyIdValidation(workerDto.getCompanyId(), workerDto.getTeamId());
        // 获取工人的目录信息
        DirectoryEntryDto directoryEntryDto = directoryService.getDirectoryEntry(workerDto.getCompanyId(), workerDto.getUserId());
        // 检查工人是否已经存在
        Worker worker = workerRepo.findByTeamIdAndUserId(workerDto.getTeamId(), workerDto.getUserId());
        if (worker != null) {
            throw new ServiceException("user is already a worker");// 如果工人已存在，抛出异常
        }

        try {
            // 创建新的工人记录并保存到数据库
            Worker workerToCreate = Worker.builder().teamId(workerDto.getTeamId()).userId(workerDto.getUserId()).build();
            workerRepo.save(workerToCreate);
        } catch (Exception ex) {
            // 如果创建工人失败，记录错误日志并抛出异常
            String errMsg = "failed to create worker in database";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        // 创建审计日志记录，记录当前用户添加了哪个工人获取当前用户ID，获取当前用户的授权信息，目标类型为"工人"，记录被创建工人的ID，所属的公司ID，所属的团队ID）
        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("worker")
                .targetId(workerDto.getUserId())
                .companyId(workerDto.getCompanyId())
                .teamId(workerDto.getTeamId())
                .build();
        // 记录添加工人的日志
        logger.info("added worker", auditLog);
        // 异步追踪创建工人的事件
        serviceHelper.trackEventAsync("worker_created");
        // 返回工人的目录信息
        return directoryEntryDto;
    }
}
