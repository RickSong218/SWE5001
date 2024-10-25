package com.ssms.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseResponse {
    private String message;
    @Builder.Default
    private com.ssms.common.api.ResultCode code = com.ssms.common.api.ResultCode.SUCCESS;

    public boolean isSuccess() {
        return code == com.ssms.common.api.ResultCode.SUCCESS;
    }
}
