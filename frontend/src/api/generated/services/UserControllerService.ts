/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApiResponseUser } from '../models/ApiResponseUser';
import type { ApiResponseVoid } from '../models/ApiResponseVoid';
import type { ChangePasswordRequest } from '../models/ChangePasswordRequest';
import type { UpdateProfileRequest } from '../models/UpdateProfileRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class UserControllerService {
    /**
     * @returns ApiResponseUser OK
     * @throws ApiError
     */
    public static getCurrentProfile(): CancelablePromise<ApiResponseUser> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/users/me',
        });
    }
    /**
     * @param requestBody
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static updateProfile(
        requestBody: UpdateProfileRequest,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/users/me',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param requestBody
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static changePassword(
        requestBody: ChangePasswordRequest,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/users/me/change-password',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
}
