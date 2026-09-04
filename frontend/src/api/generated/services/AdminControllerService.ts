/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApiResponseListUserSummaryResponse } from '../models/ApiResponseListUserSummaryResponse';
import type { ApiResponseUserDetailResponse } from '../models/ApiResponseUserDetailResponse';
import type { ApiResponseVoid } from '../models/ApiResponseVoid';
import type { UpdateRoleRequest } from '../models/UpdateRoleRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class AdminControllerService {
    /**
     * @param id
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static unlockUserAccount(
        id: number,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/admin/users/{id}/unlock',
            path: {
                'id': id,
            },
        });
    }
    /**
     * @param id
     * @param requestBody
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static changeUserRole(
        id: number,
        requestBody: UpdateRoleRequest,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/admin/users/{id}/role',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param id
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static lockUserAccount(
        id: number,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/admin/users/{id}/lock',
            path: {
                'id': id,
            },
        });
    }
    /**
     * @returns ApiResponseListUserSummaryResponse OK
     * @throws ApiError
     */
    public static getAllUsers(): CancelablePromise<ApiResponseListUserSummaryResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/admin/users',
        });
    }
    /**
     * @param id
     * @returns ApiResponseUserDetailResponse OK
     * @throws ApiError
     */
    public static getUserById(
        id: number,
    ): CancelablePromise<ApiResponseUserDetailResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/admin/users/{id}',
            path: {
                'id': id,
            },
        });
    }
    /**
     * @param id
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static deleteUserAccount(
        id: number,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/api/admin/users/{id}',
            path: {
                'id': id,
            },
        });
    }
}
