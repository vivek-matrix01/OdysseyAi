package com.OdysseyAi.demo.trip.mapper;

import com.OdysseyAi.demo.trip.dto.request.CreateExpenseRequest;
import com.OdysseyAi.demo.trip.dto.response.ExpenseResponse;
import com.OdysseyAi.demo.trip.entity.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "paidBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Expense toEntity(CreateExpenseRequest request);

    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "paidByUserId", source = "paidBy.id")
    @Mapping(
            target = "paidByName",
            expression = "java(expense.getPaidBy().getFirstName() + \" \" + expense.getPaidBy().getLastName())"
    )
    ExpenseResponse toResponse(Expense expense);
}