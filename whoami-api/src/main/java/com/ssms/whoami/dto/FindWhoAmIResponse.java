package com.ssms.whoami.dto;

import lombok.*;
import com.ssms.common.api.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FindWhoAmIResponse extends BaseResponse {
    private IAmDto iAm;
}
