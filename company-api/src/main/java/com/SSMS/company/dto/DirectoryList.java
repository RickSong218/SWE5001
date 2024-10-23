package com.SSMS.company.dto;

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
