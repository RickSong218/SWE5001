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
public class Association {
    private com.ssms.company.dto.DirectoryEntryDto account;
    @Builder.Default
    private List<com.ssms.company.dto.TeamDto> teams = new ArrayList<com.ssms.company.dto.TeamDto>();
    private Boolean admin;
}
