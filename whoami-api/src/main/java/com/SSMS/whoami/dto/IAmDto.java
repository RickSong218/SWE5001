package com.ssms.whoami.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.ssms.company.dto.AdminOfList;
import com.ssms.company.dto.WorkerOfList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IAmDto {
    private boolean support;
    private String userId;
    private WorkerOfList workerOfList;
    private AdminOfList adminOfList;
}
