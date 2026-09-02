package com.OdysseyAi.demo.trip.controller;

import com.OdysseyAi.demo.common.api.ApiResponse;
import com.OdysseyAi.demo.common.enums.ExpenseCategory;
import com.OdysseyAi.demo.trip.dto.request.CreateExpenseRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateExpenseRequest;
import com.OdysseyAi.demo.trip.dto.response.ExpenseResponse;
import com.OdysseyAi.demo.trip.dto.response.PageResponse;
import com.OdysseyAi.demo.trip.interfaces.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/trips/{tripId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @PathVariable Long tripId,
            @Valid @RequestBody CreateExpenseRequest request
    ) {

        ExpenseResponse response =
                expenseService.createExpense(
                        tripId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<ExpenseResponse>builder()
                                .status(HttpStatus.CREATED.value())
                                .message("Expense created successfully.")
                                .data(response)
                                .build()
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> getExpenses(
            @PathVariable Long tripId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) Long paidByUserId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {

        PageResponse<ExpenseResponse> response =
                expenseService.getExpenses(
                        tripId,
                        page,
                        size,
                        sortBy,
                        direction,
                        category,
                        paidByUserId,
                        startDate,
                        endDate
                );

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<ExpenseResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Expenses retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long tripId,
            @PathVariable Long expenseId,
            @Valid @RequestBody UpdateExpenseRequest request
    ) {

        ExpenseResponse response =
                expenseService.updateExpense(
                        tripId,
                        expenseId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.<ExpenseResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Expense updated successfully.")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable Long tripId,
            @PathVariable Long expenseId
    ) {

        expenseService.deleteExpense(
                tripId,
                expenseId
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Expense deleted successfully.")
                        .build()
        );
    }
}
