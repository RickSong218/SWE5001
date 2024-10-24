package com.ssms.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminEntries {
    private String companyId;
    @Builder.Default
    private List<com.ssms.company.dto.DirectoryEntryDto> admins = new ArrayList<com.ssms.company.dto.DirectoryEntryDto>();
}
