package com.ssms.company.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOfList {
    private String userId;
    @Builder.Default
    private List<CompanyDto> companies = new ArrayList<CompanyDto>();
}
