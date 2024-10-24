package com.ssms.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOfList {
    private String userId;
    @Builder.Default
    private List<com.ssms.company.dto.CompanyDto> companies = new ArrayList<com.ssms.company.dto.CompanyDto>();
}
