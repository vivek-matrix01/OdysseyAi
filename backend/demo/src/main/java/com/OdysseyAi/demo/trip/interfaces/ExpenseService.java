package com.OdysseyAi.demo.trip.interfaces;


import com.OdysseyAi.demo.common.enums.ExpenseCategory;
import com.OdysseyAi.demo.trip.dto.request.CreateExpenseRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateExpenseRequest;
import com.OdysseyAi.demo.trip.dto.response.ExpenseResponse;
import com.OdysseyAi.demo.trip.dto.response.PageResponse;

import java.time.LocalDate;

public interface ExpenseService {

    ExpenseResponse createExpense(
            Long tripId,
            CreateExpenseRequest request
    );

    PageResponse<ExpenseResponse> getExpenses(
            Long tripId,
            int page,
            int size,
            String sortBy,
            String direction,
            ExpenseCategory category,
            Long paidByUserId,
            LocalDate startDate,
            LocalDate endDate
    );

    ExpenseResponse updateExpense(
            Long tripId,
            Long expenseId,
            UpdateExpenseRequest request
    );

    void deleteExpense(
            Long tripId,
            Long expenseId
    );
}