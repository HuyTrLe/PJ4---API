package com.mytech.api.models.debt;


import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.NoArgsConstructor;


@Data

@NoArgsConstructor
@AllArgsConstructor
public class ReportDebtParam {
    private Long userId;
    private LocalDate fromDate;
    private LocalDate toDate;
}