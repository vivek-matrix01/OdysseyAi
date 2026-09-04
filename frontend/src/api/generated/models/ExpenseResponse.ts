/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type ExpenseResponse = {
    id?: number;
    tripId?: number;
    amount?: number;
    currency?: string;
    category?: ExpenseResponse.category;
    description?: string;
    expenseDate?: string;
    paidByUserId?: number;
    paidByName?: string;
};
export namespace ExpenseResponse {
    export enum category {
        TRANSPORT = 'TRANSPORT',
        ACCOMMODATION = 'ACCOMMODATION',
        FOOD = 'FOOD',
        ACTIVITIES = 'ACTIVITIES',
        SHOPPING = 'SHOPPING',
        OTHER = 'OTHER',
    }
}

