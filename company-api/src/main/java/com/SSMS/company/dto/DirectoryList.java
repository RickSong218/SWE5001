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
public class DirectoryList {
    @Builder.Default
    private List<DirectoryEntryDto> accounts = new ArrayList<DirectoryEntryDto>();
    private int limit;
    private int offset;
}
