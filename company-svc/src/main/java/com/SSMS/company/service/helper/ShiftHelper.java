package com.SSMS.company.service.helper;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.SSMS.common.api.ResultCode;
import com.SSMS.common.auditlog.LogEntry;
import com.SSMS.common.auth.AuthContext;
import com.SSMS.common.error.ServiceException;
import com.SSMS.company.dto.ShiftDto;
import com.SSMS.company.model.Shift;
import com.SSMS.company.repo.ShiftRepo;
import com.SSMS.company.service.JobService;

@Component
public class ShiftHelper {

    static final ILogger logger = SLoggerFactory.getLogger(ShiftHelper.class);

    @Autowired
    TeamService teamService;

    @Autowired
    DirectoryService directoryService;

    @Autowired
    JobService jobService;

    @Autowired
    ShiftRepo shiftRepo;

    @Autowired
    ServiceHelper serviceHelper;

    @Autowired
    ModelMapper modelMapper;

//    @Async("asyncExecutor")
//    public void updateShiftAsync(ShiftDto shiftDto) {
//        updateShift(shiftDto, true);
//    }


    public ShiftDto updateShift(ShiftDto shiftDtoToUpdate, boolean suppressNotification) {
        // validate and will throw exception if not exist
        ShiftDto orig = this.getShift(shiftDtoToUpdate.getId(), shiftDtoToUpdate.getTeamId(), shiftDtoToUpdate.getCompanyId());

        if (orig.equals(shiftDtoToUpdate)) { // no change
            return shiftDtoToUpdate;
        }

        if (!StringUtils.isEmpty(shiftDtoToUpdate.getUserId())) {
            // validate and will throw exception if not exist
            directoryService.getDirectoryEntry(shiftDtoToUpdate.getCompanyId(), shiftDtoToUpdate.getUserId());
        }

        if (!StringUtils.isEmpty(shiftDtoToUpdate.getJobId())) {
            // validate and will throw exception if not exist
            jobService.getJob(shiftDtoToUpdate.getJobId(), shiftDtoToUpdate.getCompanyId(), shiftDtoToUpdate.getTeamId());
        }

        Shift shiftToUpdate = this.convertToModel(shiftDtoToUpdate);

        try {
            shiftRepo.save(shiftToUpdate);
        } catch (Exception ex) {
            String errMsg = "could not update the shift";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("shift")
                .targetId(shiftDtoToUpdate.getId())
                .companyId(shiftDtoToUpdate.getCompanyId())
                .teamId(shiftDtoToUpdate.getTeamId())
                .originalContents(orig.toString())
                .updatedContents(shiftDtoToUpdate.toString())
                .build();

        logger.info("updated shift", auditLog);

        serviceHelper.trackEventAsync("shift_updated");
        if (!orig.isPublished() && shiftDtoToUpdate.isPublished()) {
            serviceHelper.trackEventAsync("shift_published");
        }

        if (!suppressNotification) {
            serviceHelper.updateShiftNotificationAsync(orig, shiftDtoToUpdate);
        }

        return shiftDtoToUpdate;
    }

    public ShiftDto getShift(String shiftId, String teamId, String companyId) {
        // validate and will throw exception if not exist
        teamService.getTeamWithCompanyIdValidation(companyId, teamId);

        Shift shift = shiftRepo.findShiftById(shiftId);
        if (shift == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "shift with specified id not found");
        }

        ShiftDto shiftDto = convertToDto(shift);
        shiftDto.setCompanyId(companyId);

        return shiftDto;
    }

    public ShiftDto convertToDto(Shift shift) {
        return modelMapper.map(shift, ShiftDto.class);
    }

    public Shift convertToModel(ShiftDto shiftDto) {
        return modelMapper.map(shiftDto, Shift.class);
    }
}
