package com.SSMS.company.service;

import com.SSMS.common.api.ResultCode;
import com.SSMS.common.auditlog.LogEntry;
import com.SSMS.common.auth.AuthContext;
import com.SSMS.common.error.ServiceException;
import com.SSMS.company.dto.CompanyDto;
import com.SSMS.company.dto.CompanyList;
import com.SSMS.company.model.Company;
import com.SSMS.company.repo.CompanyRepo;
import com.SSMS.company.service.helper.ServiceHelper;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class CompanyService {

    static final ILogger logger = SLoggerFactory.getLogger(CompanyService.class);

    @Autowired
    private CompanyRepo companyRepo;

    @Autowired
    private ServiceHelper serviceHelper;

    @Autowired
    private ModelMapper modelMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public CompanyDto createCompany(CompanyDto companyDto) {
        Company company = this.convertToModel(companyDto);

        Company savedCompany = null;
        try {
            savedCompany = companyRepo.save(company);
        } catch (Exception ex) {
            String errMsg = "could not create company";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("company")
                .targetId(company.getId())
                .companyId(company.getId())
                .teamId("")
                .updatedContents(company.toString())
                .build();

        logger.info("created company", auditLog);

        serviceHelper.trackEventAsync("company_created");

        return this.convertToDto(savedCompany);
    }

    public CompanyList listCompanies(int offset, int limit) {

        if (limit <= 0) {
            limit = 20;
        }

        Pageable pageRequest = PageRequest.of(offset, limit);
        Page<Company> companyPage = null;
        try {
            companyPage = companyRepo.findAll(pageRequest);
        } catch (Exception ex) {
            String errMsg = "fail to query database for company list";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        List<CompanyDto> companyDtoList = companyPage.getContent().stream().map(company -> convertToDto(company)).collect(toList());

        return CompanyList.builder()
                .limit(limit)
                .offset(offset)
                .companies(companyDtoList)
                .build();
}

    public CompanyDto getCompany(String companyId) {

        Company company = companyRepo.findCompanyById(companyId);
        if (company == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "Company not found");
        }

        return this.convertToDto(company);

    }

    public CompanyDto updateCompany(CompanyDto companyDto) {
        Company existingCompany = companyRepo.findCompanyById(companyDto.getId());
        if (existingCompany == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "Company not found");
        }
        entityManager.detach(existingCompany);

        Company companyToUpdate = this.convertToModel(companyDto);
        Company updatedCompany = null;
        try {
            updatedCompany = companyRepo.save(companyToUpdate);
        } catch (Exception ex) {
            String errMsg = "could not update the companyDto";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        LogEntry auditLog = LogEntry.builder()
                .currentUserId(AuthContext.getUserId())
                .authorization(AuthContext.getAuthz())
                .targetType("company")
                .targetId(companyToUpdate.getId())
                .companyId(companyToUpdate.getId())
                .teamId("")
                .originalContents(existingCompany.toString())
                .updatedContents(updatedCompany.toString())
                .build();

        logger.info("updated company", auditLog);

        serviceHelper.trackEventAsync("company_updated");

        return this.convertToDto(updatedCompany);
    }

    private CompanyDto convertToDto(Company company) {
        return modelMapper.map(company, CompanyDto.class);
    }

    private Company convertToModel(CompanyDto companyDto) {
        return modelMapper.map(companyDto, Company.class);
    }
}
