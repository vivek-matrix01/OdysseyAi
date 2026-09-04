/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type CreateExpenseRequest = {
    amount: number;
    currency: string;
    category: CreateExpenseRequest.category;
    description?: string;
    expenseDate: string;
};
export namespace CreateExpenseRequest {
    export enum category {
        TRANSPORT = 'TRANSPORT',
        ACCOMMODATION = 'ACCOMMODATION',
        FOOD = 'FOOD',
        ACTIVITIES = 'ACTIVITIES',
        SHOPPING = 'SHOPPING',
        OTHER = 'OTHER',
    }
}

