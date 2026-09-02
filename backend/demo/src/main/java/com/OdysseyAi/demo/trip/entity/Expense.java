package com.OdysseyAi.demo.trip.entity;



import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.common.entity.BaseEntity;
import com.OdysseyAi.demo.common.enums.ExpenseCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "expenses",
        indexes = {
                @Index(name = "idx_expense_trip", columnList = "trip_id"),
                @Index(name = "idx_expense_payer", columnList = "paid_by")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Expense extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "trip_id",
            nullable = false
    )
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "paid_by",
            nullable = false
    )
    private User paidBy;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseCategory category;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate expenseDate;

    public void assignTrip(Trip trip) {
        this.trip = trip;
    }

    public void assignPaidBy(User user) {
        this.paidBy = user;
    }

    public void updateDetails(
            BigDecimal amount,
            String currency,
            ExpenseCategory category,
            String description,
            LocalDate expenseDate
    ) {
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.description = description;
        this.expenseDate = expenseDate;
    }
}