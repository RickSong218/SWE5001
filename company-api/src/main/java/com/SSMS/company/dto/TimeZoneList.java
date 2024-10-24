package com.ssms.company.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeZoneList {
    @Singular
    private List<String> timezones;
}
