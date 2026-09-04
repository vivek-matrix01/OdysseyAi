/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApiResponseExpenseResponse } from '../models/ApiResponseExpenseResponse';
import type { ApiResponsePageResponseExpenseResponse } from '../models/ApiResponsePageResponseExpenseResponse';
import type { ApiResponseVoid } from '../models/ApiResponseVoid';
import type { CreateExpenseRequest } from '../models/CreateExpenseRequest';
import type { UpdateExpenseRequest } from '../models/UpdateExpenseRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ExpenseControllerService {
    /**
     * @param tripId
     * @param expenseId
     * @param requestBody
     * @returns ApiResponseExpenseResponse OK
     * @throws ApiError
     */
    public static updateExpense(
        tripId: number,
        expenseId: number,
        requestBody: UpdateExpenseRequest,
    ): CancelablePromise<ApiResponseExpenseResponse> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/trips/{tripId}/expenses/{expenseId}',
            path: {
                'tripId': tripId,
                'expenseId': expenseId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param tripId
     * @param expenseId
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static deleteExpense(
        tripId: number,
        expenseId: number,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/api/trips/{tripId}/expenses/{expenseId}',
            path: {
                'tripId': tripId,
                'expenseId': expenseId,
            },
        });
    }
    /**
     * @param tripId
     * @param page
     * @param size
     * @param sortBy
     * @param direction
     * @param category
     * @param paidByUserId
     * @param startDate
     * @param endDate
     * @returns ApiResponsePageResponseExpenseResponse OK
     * @throws ApiError
     */
    public static getExpenses(
        tripId: number,
        page?: number,
        size: number = 10,
        sortBy: string = 'expenseDate',
        direction: string = 'desc',
        category?: 'TRANSPORT' | 'ACCOMMODATION' | 'FOOD' | 'ACTIVITIES' | 'SHOPPING' | 'OTHER',
        paidByUserId?: number,
        startDate?: string,
        endDate?: string,
    ): CancelablePromise<ApiResponsePageResponseExpenseResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/trips/{tripId}/expenses',
            path: {
                'tripId': tripId,
            },
            query: {
                'page': page,
                'size': size,
                'sortBy': sortBy,
                'direction': direction,
                'category': category,
                'paidByUserId': paidByUserId,
                'startDate': startDate,
                'endDate': endDate,
            },
        });
    }
    /**
     * @param tripId
     * @param requestBody
     * @returns ApiResponseExpenseResponse OK
     * @throws ApiError
     */
    public static createExpense(
        tripId: number,
        requestBody: CreateExpenseRequest,
    ): CancelablePromise<ApiResponseExpenseResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/trips/{tripId}/expenses',
            path: {
                'tripId': tripId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
}
