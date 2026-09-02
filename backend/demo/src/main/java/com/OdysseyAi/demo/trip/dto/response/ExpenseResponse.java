package com.OdysseyAi.demo.trip.dto.response;

import com.OdysseyAi.demo.common.enums.ExpenseCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;

    private Long tripId;

    private BigDecimal amount;

    private String currency;

    private ExpenseCategory category;

    private String description;

    private LocalDate expenseDate;

    private Long paidByUserId;

    private String paidByName;
}