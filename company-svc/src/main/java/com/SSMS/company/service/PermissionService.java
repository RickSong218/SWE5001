package com.ssms.company.service;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.apache.tomcat.jni.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.ssms.common.auth.AuthContext;
import com.ssms.common.auth.PermissionDeniedException;
import com.ssms.company.model.Worker;
import com.ssms.company.repo.WorkerRepo;
import com.ssms.company.service.helper.ServiceHelper;


@Service
public class PermissionService {
    // 定义静态日志记录器，用于记录权限检查相关的日志
    static final ILogger logger = SLoggerFactory.getLogger(PermissionService.class);

    @Autowired
    private SentryClient sentryClient; //SentryClient，用于记录错误日志和异常处理

    @Autowired
    AdminRepo adminRepo;

    @Autowired
    WorkerRepo workerRepo;

    @Autowired
    DirectoryRepo directoryRepo;

    @Autowired
    ServiceHelper serviceHelper;
    // 检查当前用户是否是指定公司的管理员
    public void checkPermissionCompanyAdmin(String companyId) {
        // 获取当前用户的ID
        String currentUserId = checkAndGetCurrentUserId();
        Admin admin = null;
        try {
            // 通过公司ID和用户ID查找该用户是否是该公司的管理员
            admin = adminRepo.findByCompanyIdAndUserId(companyId, currentUserId);
        } catch (Exception ex) {
            // 通过公司ID和用户ID查找该用户是否是该公司的管理员
            String errMsg = "failed to check company admin permissions";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        // 如果该用户不是管理员，则抛出权限拒绝异常
        if (admin == null) {
            throw new PermissionDeniedException("you do not have admin access to this service");
        }
    }

    // 检查用户是否是指定团队的工人或指定公司的管理员
    public void checkPermissionTeamWorker(String companyId, String teamId) {
        String currentUserId = checkAndGetCurrentUserId();  // 获取当前用户的ID

        // 首先检查该用户是否是公司管理员
        try {
            Admin admin = adminRepo.findByCompanyIdAndUserId(companyId, currentUserId);
            if (admin != null) // 如果是管理员，则允许访问，不进行进一步检查
            {
                return;
            }
        } catch (Exception ex) {
            // 捕获异常，记录错误日志并抛出自定义异常
            String errMsg = "failed to check company admin permissions";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        // 如果用户不是管理员，则检查用户是否是团队的成员（工人）
        Worker worker = null;
        try {
            worker = workerRepo.findByTeamIdAndUserId(teamId, currentUserId);
        } catch (Exception ex) {
            // 捕获异常，记录错误日志并抛出自定义异常
            String errMsg = "failed to check teamDto member permissions";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        // 如果该用户不是团队的成员，则抛出权限拒绝异常
        if (worker == null) {
            throw new PermissionDeniedException("you are not associated with this company");
        }
    }

    // 检查用户是否存在于指定公司的目录中，用户可能已经不是团队成员（如前员工）
    public void checkPermissionCompanyDirectory(String companyId) {
        String currentUserId = checkAndGetCurrentUserId();// 获取当前用户的ID

        Directory directory = null;
        try {
            // 查找该用户是否存在于公司的目录中
            directory = directoryRepo.findByCompanyIdAndUserId(companyId, currentUserId);
        } catch (Exception ex) {
            // 捕获异常，记录错误日志并抛出自定义异常
            String errMsg = "failed to check directory existence";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        // 如果该用户不在公司的目录中，则抛出权限拒绝异常
        if (directory == null) {
            throw new PermissionDeniedException("you are not associated with this company");
        }
    }
    // 私有方法，获取当前用户ID并进行验证，如果找不到用户ID则抛出异常
    private String checkAndGetCurrentUserId() {
        String currentUserId = AuthContext.getUserId();// 从AuthContext中获取当前用户的ID
        // 如果用户ID为空，则记录错误并抛出异常
        if (StringUtils.isEmpty(currentUserId)) {
            String errMsg = "failed to find current user id";
            serviceHelper.handleErrorAndThrowException(logger, errMsg);
        }
        return currentUserId;// 返回当前用户的ID
    }
}
