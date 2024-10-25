package com.ssms.company.service;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ssms.common.api.ResultCode;
import com.ssms.common.auditlog.LogEntry;
import com.ssms.common.auth.AuthContext;
import com.ssms.common.error.ServiceException;
import com.ssms.company.dto.CreateJobRequest;
import com.ssms.company.dto.JobDto;
import com.ssms.company.dto.JobList;
import com.ssms.company.model.Job;
import com.ssms.company.repo.JobRepo;
import com.ssms.company.service.helper.ServiceHelper;

import java.util.List;

@Service
public class JobService {
    // 定义静态日志记录器，用于记录与Job操作相关的日志
    static final ILogger logger = SLoggerFactory.getLogger(JobService.class);

    @Autowired
    JobRepo jobRepo;

    @Autowired
    TeamService teamService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    ServiceHelper serviceHelper;
    // 创建新的 Job
    public JobDto createJob(CreateJobRequest request) {
        // 验证团队是否存在，不存在则抛出异常
        teamService.getTeamWithCompanyIdValidation(request.getCompanyId(), request.getTeamId());
        // 使用请求的数据构建 Job 对象
        Job job = Job.builder()
                .name(request.getName())
                .color(request.getColor())
                .teamId(request.getTeamId())
                .build();
        // 尝试保存 Job 对象到数据库
        try {
            jobRepo.save(job);
        } catch(Exception ex) {
            // 捕获异常并记录日志，抛出自定义的服务异常
            String errMsg = "could not create job";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        // 创建审计日志记录，记录创建的 Job 信息
        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("job")
                .targetId(job.getId())
                .companyId(request.getCompanyId())
                .teamId(job.getTeamId())
                .updatedContents(job.toString())
                .build();
        // 记录 Job 创建的日志
        logger.info("created job", auditLog);
        // 异步跟踪 Job 创建事件
        serviceHelper.trackEventAsync("job_created");
        // 将 Job 转换为 JobDto，并设置公司ID
        JobDto jobDto = this.convertToDto(job);
        jobDto.setCompanyId(request.getCompanyId());
        // 返回创建的 Job 的数据传输对象
        return jobDto;
    }
    // 列出某个团队的所有 Job
    public JobList listJobs(String companyId, String teamId) {
        // 验证团队是否存在，不存在则抛出异常
        teamService.getTeamWithCompanyIdValidation(companyId, teamId);
        // 创建 JobList 对象
        JobList jobList = JobList.builder().build();
        // 从数据库中根据团队ID查找所有 Job
        List<Job> jobs = jobRepo.findJobByTeamId(teamId);
        // 将每个 Job 转换为 JobDto 并添加到 JobList 中
        for (Job job : jobs) {
            JobDto jobDto = this.convertToDto(job);
            jobDto.setCompanyId(companyId);
            jobList.getJobs().add(jobDto);
        }
        // 返回包含所有 Job 的 JobList 对象
        return jobList;
    }
    // 根据 Job ID 获取具体的 Job 信息
    public JobDto getJob(String jobId, String companyId, String teamId) {
        // 验证团队是否存在，不存在则抛出异常
        teamService.getTeamWithCompanyIdValidation(companyId, teamId);
        // 从数据库中查找指定ID的 Job
        Job job = jobRepo.findJobById(jobId);
        if (job == null) {
            // 如果 Job 不存在，抛出自定义的服务异常
            throw new ServiceException(ResultCode.NOT_FOUND, "job not found");
        }
        // 将 Job 转换为 JobDto，并设置公司ID
        JobDto jobDto = this.convertToDto(job);
        jobDto.setCompanyId(companyId);
        // 返回 Job 的数据传输对象
        return jobDto;
    }
    // 更新现有的 Job
    public JobDto updateJob(JobDto jobDtoToUpdate) {
        // 验证团队是否存在，不存在则抛出异常
        teamService.getTeamWithCompanyIdValidation(jobDtoToUpdate.getCompanyId(), jobDtoToUpdate.getTeamId());
        // 获取原始的 Job 信息
        JobDto orig = this.getJob(jobDtoToUpdate.getId(), jobDtoToUpdate.getCompanyId(), jobDtoToUpdate.getTeamId());
        // 将更新的 JobDto 转换为 Job 对象
        Job jobToUpdate = convertToModel(jobDtoToUpdate);
        // 尝试保存更新后的 Job 对象到数据库
        try {
            jobRepo.save(jobToUpdate);
        } catch (Exception ex) {
            // 捕获异常并记录日志，抛出自定义的服务异常
            String errMsg = "could not update job";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        // 创建审计日志记录，记录 Job 的更新操
        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("job")
                .targetId(jobDtoToUpdate.getId())
                .companyId(jobDtoToUpdate.getCompanyId())
                .teamId(jobDtoToUpdate.getTeamId())
                .originalContents(orig.toString())
                .updatedContents(jobDtoToUpdate.toString())
                .build();
        // 记录 Job 更新的日志
        logger.info("updated job", auditLog);
        // 异步跟踪 Job 更新事件
        serviceHelper.trackEventAsync("job_updated");
        // 返回更新后的 JobDto 对象
        return jobDtoToUpdate;
    }
    // 将 Job 对象转换为 JobDto 对象
    JobDto convertToDto(Job job) {
        return modelMapper.map(job, JobDto.class);
    }
    // 将 JobDto 对象转换为 Job 对象
    Job convertToModel(JobDto jobDto) {
        return modelMapper.map(jobDto, Job.class);
    }
}
