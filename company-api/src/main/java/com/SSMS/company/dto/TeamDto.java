package com.SSMS.company.dto;

import com.SSMS.common.validation.DayOfWeek;
import com.SSMS.common.validation.Timezone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamDto {
    @NotBlank
    private String id;
    @NotBlank
    private String companyId;
    @NotBlank
    private String name;
    private boolean archived;
    @Timezone
    @NotBlank
    private String timezone;
    @DayOfWeek
    @NotBlank
    private String dayWeekStarts;
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    @NotBlank
    private String color;
}
