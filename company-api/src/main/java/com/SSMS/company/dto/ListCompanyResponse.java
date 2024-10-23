package com.SSMS.company.dto;

import com.SSMS.common.api.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ListCompanyResponse extends BaseResponse {
    private CompanyList companyList;
}
