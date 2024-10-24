package com.ssms.company.repo;

import com.ssms.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepo extends JpaRepository<Company, String> {
    Company findCompanyById(String id);
}
