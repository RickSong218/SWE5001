package com.SSMS.company.dto;

import com.SSMS.common.validation.DayOfWeek;
import com.SSMS.common.validation.Group1;
import com.SSMS.common.validation.Group2;
import com.SSMS.common.validation.Timezone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyDto {
    @NotBlank(groups = {Group1.class})
    private String id;
    @NotBlank(groups = {Group1.class, Group2.class})
    private String name;
    private boolean archived;
    @Timezone(groups = {Group1.class, Group2.class})
    @NotBlank(groups = {Group1.class, Group2.class})
    private String defaultTimezone;
    @DayOfWeek(groups = {Group1.class, Group2.class})
    @NotBlank(groups = {Group1.class, Group2.class})
    private String defaultDayWeekStarts;
}
