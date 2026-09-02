package com.OdysseyAi.demo.trip.service;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.interfaces.AuthenticationFacade;
import com.OdysseyAi.demo.common.enums.ExpenseCategory;
import com.OdysseyAi.demo.common.exception.customexception.BadRequestException;
import com.OdysseyAi.demo.common.exception.customexception.ResourceNotFoundException;
import com.OdysseyAi.demo.trip.dto.request.CreateExpenseRequest;
import com.OdysseyAi.demo.trip.dto.request.UpdateExpenseRequest;
import com.OdysseyAi.demo.trip.dto.response.ExpenseResponse;
import com.OdysseyAi.demo.trip.dto.response.PageResponse;
import com.OdysseyAi.demo.trip.entity.Expense;
import com.OdysseyAi.demo.trip.entity.Trip;
import com.OdysseyAi.demo.trip.entity.TripMember;
import com.OdysseyAi.demo.trip.interfaces.ExpenseService;
import com.OdysseyAi.demo.trip.interfaces.TripAccessService;
import com.OdysseyAi.demo.trip.mapper.ExpenseMapper;
import com.OdysseyAi.demo.trip.repository.ExpenseRepository;
import com.OdysseyAi.demo.trip.repository.TripMemberRepository;
import com.OdysseyAi.demo.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final AuthenticationFacade authenticationFacade;
    private final TripAccessService tripAccessService;
    private final ExpenseMapper expenseMapper;

    @Override
    public ExpenseResponse createExpense(
            Long tripId,
            CreateExpenseRequest request
    ) {

        User currentUser =
                authenticationFacade.getCurrentUser();

        Trip trip = findTrip(tripId);

        tripAccessService.requireEditor(
                trip,
                currentUser
        );

        validateExpenseDate(
                request.getExpenseDate(),
                trip
        );

        Expense expense =
                expenseMapper.toEntity(request);

        expense.assignTrip(trip);
        expense.assignPaidBy(currentUser);

        Expense savedExpense =
                expenseRepository.save(expense);

        return expenseMapper.toResponse(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getExpenses(
            Long tripId,
            int page,
            int size,
            String sortBy,
            String direction,
            ExpenseCategory category,
            Long paidByUserId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        User currentUser =
                authenticationFacade.getCurrentUser();

        Trip trip = findTrip(tripId);

        tripAccessService.requireViewer(
                trip,
                currentUser
        );

        validatePagination(
                page,
                size
        );

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "expenseDate";
        }

        if (direction == null || direction.isBlank()) {
            direction = "desc";
        }

        String validatedSortField =
                validateSortField(sortBy);

        Sort.Direction sortDirection;

        try {
            sortDirection =
                    Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {

            throw new BadRequestException(
                    "Sort direction must be either 'asc' or 'desc'."
            );
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                sortDirection,
                                validatedSortField
                        )
                );

        validateDateRange(
                startDate,
                endDate
        );

        Page<Expense> expensePage;

        boolean hasCategory =
                category != null;

        boolean hasPayer =
                paidByUserId != null;

        boolean hasDateRange =
                startDate != null &&
                        endDate != null;

        /*
         * We currently keep the repository combinations explicit.
         * This makes the generated SQL predictable.
         */

        if (hasCategory && hasDateRange) {

            expensePage =
                    expenseRepository
                            .findAllByTripAndCategoryAndExpenseDateBetween(
                                    trip,
                                    category,
                                    startDate,
                                    endDate,
                                    pageable
                            );

        } else if (hasCategory) {

            expensePage =
                    expenseRepository
                            .findAllByTripAndCategory(
                                    trip,
                                    category,
                                    pageable
                            );

        } else if (hasPayer) {

            User payer =
                    findTripMemberUser(
                            trip,
                            paidByUserId
                    );

            expensePage =
                    expenseRepository
                            .findAllByTripAndPaidBy(
                                    trip,
                                    payer,
                                    pageable
                            );

        } else if (hasDateRange) {

            expensePage =
                    expenseRepository
                            .findAllByTripAndExpenseDateBetween(
                                    trip,
                                    startDate,
                                    endDate,
                                    pageable
                            );

        } else {

            expensePage =
                    expenseRepository.findAllByTrip(
                            trip,
                            pageable
                    );
        }

        return toPageResponse(expensePage);
    }

    @Override
    public ExpenseResponse updateExpense(
            Long tripId,
            Long expenseId,
            UpdateExpenseRequest request
    ) {

        User currentUser =
                authenticationFacade.getCurrentUser();

        Trip trip = findTrip(tripId);

        tripAccessService.requireEditor(
                trip,
                currentUser
        );

        Expense expense =
                findExpense(
                        expenseId,
                        trip
                );

        validateExpenseDate(
                request.getExpenseDate(),
                trip
        );

        expense.updateDetails(
                request.getAmount(),
                request.getCurrency(),
                request.getCategory(),
                request.getDescription(),
                request.getExpenseDate()
        );

        return expenseMapper.toResponse(expense);
    }

    @Override
    public void deleteExpense(
            Long tripId,
            Long expenseId
    ) {

        User currentUser =
                authenticationFacade.getCurrentUser();

        Trip trip = findTrip(tripId);

        tripAccessService.requireEditor(
                trip,
                currentUser
        );

        Expense expense =
                findExpense(
                        expenseId,
                        trip
                );

        expenseRepository.delete(expense);
    }

    private Trip findTrip(Long tripId) {

        return tripRepository
                .findById(tripId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trip not found."
                        )
                );
    }

    private Expense findExpense(
            Long expenseId,
            Trip trip
    ) {

        return expenseRepository
                .findByIdAndTrip(
                        expenseId,
                        trip
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found."
                        )
                );
    }

    private User findTripMemberUser(
            Trip trip,
            Long userId
    ) {

        if (trip.getUser().getId().equals(userId)) {
            return trip.getUser();
        }

        TripMember member =
                tripMemberRepository
                        .findByTripAndUserId(
                                trip,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User is not a member of this trip."
                                )
                        );

        return member.getUser();
    }

    private void validateExpenseDate(
            LocalDate expenseDate,
            Trip trip
    ) {

        if (expenseDate == null) {
            throw new BadRequestException(
                    "Expense date is required."
            );
        }

        if (expenseDate.isBefore(trip.getStartDate())
                || expenseDate.isAfter(trip.getEndDate())) {

            throw new BadRequestException(
                    "Expense date must fall within the trip dates."
            );
        }
    }

    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {

            throw new BadRequestException(
                    "End date cannot be before start date."
            );
        }
    }

    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {
            throw new BadRequestException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException(
                    "Page size must be between 1 and 100."
            );
        }
    }

    private String validateSortField(
            String sortBy
    ) {

        return switch (sortBy) {

            case "createdAt",
                 "updatedAt",
                 "amount",
                 "category",
                 "expenseDate",
                 "currency" -> sortBy;

            default -> throw new BadRequestException(
                    "Invalid expense sort field."
            );
        };
    }

    private PageResponse<ExpenseResponse> toPageResponse(
            Page<Expense> expensePage
    ) {

        return PageResponse.<ExpenseResponse>builder()
                .content(
                        expensePage.getContent()
                                .stream()
                                .map(expenseMapper::toResponse)
                                .toList()
                )
                .page(expensePage.getNumber())
                .size(expensePage.getSize())
                .totalElements(
                        expensePage.getTotalElements()
                )
                .totalPages(
                        expensePage.getTotalPages()
                )
                .first(
                        expensePage.isFirst()
                )
                .last(
                        expensePage.isLast()
                )
                .build();
    }
}