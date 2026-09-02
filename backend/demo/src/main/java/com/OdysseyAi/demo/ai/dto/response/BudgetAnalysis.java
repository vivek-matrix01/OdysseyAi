package com.OdysseyAi.demo.ai.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAnalysis {

    private BigDecimal budget;

    private BigDecimal estimatedTotalCost;

    private BigDecimal remainingBudget;

    private BigDecimal budgetUsedPercentage;

    private Boolean withinBudget;

    private String message;
}