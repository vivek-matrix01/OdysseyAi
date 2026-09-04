/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApiResponseActivityResponse } from '../models/ApiResponseActivityResponse';
import type { ApiResponseItineraryDayResponse } from '../models/ApiResponseItineraryDayResponse';
import type { ApiResponseItineraryResponse } from '../models/ApiResponseItineraryResponse';
import type { ApiResponseVoid } from '../models/ApiResponseVoid';
import type { CreateActivityRequest } from '../models/CreateActivityRequest';
import type { CreateItineraryDayRequest } from '../models/CreateItineraryDayRequest';
import type { CreateItineraryRequest } from '../models/CreateItineraryRequest';
import type { UpdateActivityRequest } from '../models/UpdateActivityRequest';
import type { UpdateItineraryDayRequest } from '../models/UpdateItineraryDayRequest';
import type { UpdateItineraryRequest } from '../models/UpdateItineraryRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ItineraryControllerService {
    /**
     * @param tripId
     * @returns ApiResponseItineraryResponse OK
     * @throws ApiError
     */
    public static getItinerary(
        tripId: number,
    ): CancelablePromise<ApiResponseItineraryResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/trips/{tripId}/itinerary',
            path: {
                'tripId': tripId,
            },
        });
    }
    /**
     * @param tripId
     * @param requestBody
     * @returns ApiResponseItineraryResponse OK
     * @throws ApiError
     */
    public static updateItinerary(
        tripId: number,
        requestBody: UpdateItineraryRequest,
    ): CancelablePromise<ApiResponseItineraryResponse> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/trips/{tripId}/itinerary',
            path: {
                'tripId': tripId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param tripId
     * @param requestBody
     * @returns ApiResponseItineraryResponse OK
     * @throws ApiError
     */
    public static createItinerary(
        tripId: number,
        requestBody: CreateItineraryRequest,
    ): CancelablePromise<ApiResponseItineraryResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/trips/{tripId}/itinerary',
            path: {
                'tripId': tripId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param tripId
     * @param dayId
     * @param requestBody
     * @returns ApiResponseItineraryDayResponse OK
     * @throws ApiError
     */
    public static updateDay(
        tripId: number,
        dayId: number,
        requestBody: UpdateItineraryDayRequest,
    ): CancelablePromise<ApiResponseItineraryDayResponse> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/trips/{tripId}/itinerary/days/{dayId}',
            path: {
                'tripId': tripId,
                'dayId': dayId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param tripId
     * @param dayId
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static deleteDay(
        tripId: number,
        dayId: number,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/api/trips/{tripId}/itinerary/days/{dayId}',
            path: {
                'tripId': tripId,
                'dayId': dayId,
            },
        });
    }
    /**
     * @param tripId
     * @param dayId
     * @param activityId
     * @param requestBody
     * @returns ApiResponseActivityResponse OK
     * @throws ApiError
     */
    public static updateActivity(
        tripId: number,
        dayId: number,
        activityId: number,
        requestBody: UpdateActivityRequest,
    ): CancelablePromise<ApiResponseActivityResponse> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/trips/{tripId}/itinerary/days/{dayId}/activities/{activityId}',
            path: {
                'tripId': tripId,
                'dayId': dayId,
                'activityId': activityId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param tripId
     * @param dayId
     * @param activityId
     * @returns ApiResponseVoid OK
     * @throws ApiError
     */
    public static deleteActivity(
        tripId: number,
        dayId: number,
        activityId: number,
    ): CancelablePromise<ApiResponseVoid> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/api/trips/{tripId}/itinerary/days/{dayId}/activities/{activityId}',
            path: {
                'tripId': tripId,
                'dayId': dayId,
                'activityId': activityId,
            },
        });
    }
    /**
     * @param tripId
     * @param requestBody
     * @returns ApiResponseItineraryDayResponse OK
     * @throws ApiError
     */
    public static addDay(
        tripId: number,
        requestBody: CreateItineraryDayRequest,
    ): CancelablePromise<ApiResponseItineraryDayResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/trips/{tripId}/itinerary/days',
            path: {
                'tripId': tripId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param tripId
     * @param dayId
     * @param requestBody
     * @returns ApiResponseActivityResponse OK
     * @throws ApiError
     */
    public static addActivity(
        tripId: number,
        dayId: number,
        requestBody: CreateActivityRequest,
    ): CancelablePromise<ApiResponseActivityResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/trips/{tripId}/itinerary/days/{dayId}/activities',
            path: {
                'tripId': tripId,
                'dayId': dayId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
}
