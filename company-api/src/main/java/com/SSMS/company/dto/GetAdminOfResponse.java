package com.SSMS.company.dto;

import com.SSMS.common.api.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GetAdminOfResponse extends BaseResponse {
    private AdminOfList adminOfList;
}
