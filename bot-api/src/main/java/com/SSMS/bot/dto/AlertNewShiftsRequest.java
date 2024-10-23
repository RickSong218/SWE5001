package com.SSMS.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.SSMS.company.dto.ShiftDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlertNewShiftsRequest {
    @NotBlank
    private String userId;
    @NotNull
    @Builder.Default
    private List<ShiftDto> newShifts = new ArrayList<>();
}
