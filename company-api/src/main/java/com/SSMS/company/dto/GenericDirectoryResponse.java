package com.ssms.company.dto;

import com.ssms.common.api.BaseResponse;
import com.ssms.company.dto.DirectoryEntryDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GenericDirectoryResponse extends BaseResponse {
    private DirectoryEntryDto directoryEntry;
}
