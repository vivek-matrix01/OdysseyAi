/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApiResponsePageResponseTripResponse } from '../models/ApiResponsePageResponseTripResponse';
import type { ApiResponseTripResponse } from '../models/ApiResponseTripResponse';
import type { ApiResponseVoid } from '../models/ApiResponseVoid';
import type { CreateTripRequest } from '../models/CreateTripRequest';
import type { UpdateTripRequest } from '../models/UpdateTripRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class TripControllerService {
    /**
     * @param id
     * @returns ApiResponseTripResponse OK
     * @throws ApiError
     */
    public static getMyTrip(
        id: number,
    ): CancelablePromise<ApiResponseTripResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/trips/{id}',
            path: {
                'id': id,
            },
        });
    }
    /**
     * @param id
     * @param requestBody
     * @returns ApiResponseTripResponse OK
     * @throws ApiError
     */
    public static updateMyTrip(
        id: number,
        requestBody: UpdateTripRequest,
    ): CancelablePromise<ApiResponseTripResponse> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/trips/{id}',
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
    public static deleteMyTrip(
        id: number,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/api/trips/{id}',
            path: {
                'id': id,
            },
        });
    }
    /**
     * @param page
     * @param size
     * @param sortBy
     * @param direction
     * @param status
     * @param destination
     * @returns ApiResponsePageResponseTripResponse OK
     * @throws ApiError
     */
    public static getMyTrips(
        page?: number,
        size: number = 10,
        sortBy: string = 'createdAt',
        direction: string = 'desc',
        status?: 'PLANNED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED',
        destination?: string,
    ): CancelablePromise<ApiResponsePageResponseTripResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/trips',
            query: {
                'page': page,
                'size': size,
                'sortBy': sortBy,
                'direction': direction,
                'status': status,
                'destination': destination,
            },
        });
    }
    /**
     * @param requestBody
     * @returns ApiResponseTripResponse OK
     * @throws ApiError
     */
    public static createTrip(
        requestBody: CreateTripRequest,
    ): CancelablePromise<ApiResponseTripResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/trips',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param id
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static startTrip(
        id: number,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/trips/{id}/start',
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
    public static completeTrip(
        id: number,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/trips/{id}/complete',
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
    public static cancelTrip(
        id: number,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/trips/{id}/cancel',
            path: {
                'id': id,
            },
        });
    }
}
