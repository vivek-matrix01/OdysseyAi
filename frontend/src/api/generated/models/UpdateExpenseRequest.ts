/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type UpdateExpenseRequest = {
    amount: number;
    currency: string;
    category: UpdateExpenseRequest.category;
    description?: string;
    expenseDate: string;
};
export namespace UpdateExpenseRequest {
    export enum category {
        TRANSPORT = 'TRANSPORT',
        ACCOMMODATION = 'ACCOMMODATION',
        FOOD = 'FOOD',
        ACTIVITIES = 'ACTIVITIES',
        SHOPPING = 'SHOPPING',
        OTHER = 'OTHER',
    }
}

