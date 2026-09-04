/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AddTripMemberRequest } from '../models/AddTripMemberRequest';
import type { ApiResponseListTripMemberResponse } from '../models/ApiResponseListTripMemberResponse';
import type { ApiResponseTripMemberResponse } from '../models/ApiResponseTripMemberResponse';
import type { ApiResponseVoid } from '../models/ApiResponseVoid';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class TripMemberControllerService {
    /**
     * @param tripId
     * @param memberId
     * @param role
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static updateRole(
        tripId: number,
        memberId: number,
        role: 'VIEWER' | 'EDITOR',
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/trips/{tripId}/members/{memberId}/role',
            path: {
                'tripId': tripId,
                'memberId': memberId,
            },
            query: {
                'role': role,
            },
        });
    }
    /**
     * @param tripId
     * @returns ApiResponseListTripMemberResponse OK
     * @throws ApiError
     */
    public static getMembers(
        tripId: number,
    ): CancelablePromise<ApiResponseListTripMemberResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/trips/{tripId}/members',
            path: {
                'tripId': tripId,
            },
        });
    }
    /**
     * @param tripId
     * @param requestBody
     * @returns ApiResponseTripMemberResponse OK
     * @throws ApiError
     */
    public static addMember(
        tripId: number,
        requestBody: AddTripMemberRequest,
    ): CancelablePromise<ApiResponseTripMemberResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/trips/{tripId}/members',
            path: {
                'tripId': tripId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param tripId
     * @param memberId
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static removeMember(
        tripId: number,
        memberId: number,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/api/trips/{tripId}/members/{memberId}',
            path: {
                'tripId': tripId,
                'memberId': memberId,
            },
        });
    }
}
