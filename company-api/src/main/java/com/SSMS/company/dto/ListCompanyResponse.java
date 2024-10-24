package com.ssms.company.dto;

import com.ssms.common.api.BaseResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ListCompanyResponse extends BaseResponse {
    private CompanyList companyList;
}
