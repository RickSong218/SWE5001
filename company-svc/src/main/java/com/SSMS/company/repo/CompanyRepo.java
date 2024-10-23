package com.SSMS.company.repo;

import com.SSMS.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepo extends JpaRepository<Company, String> {
    Company findCompanyById(String id);
}
