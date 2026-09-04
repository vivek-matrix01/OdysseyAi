/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApiResponseItineraryResponse } from '../models/ApiResponseItineraryResponse';
import type { GenerateItineraryRequest } from '../models/GenerateItineraryRequest';
import type { GenerateSelectedItineraryRequest } from '../models/GenerateSelectedItineraryRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class AiControllerService {
    /**
     * @param tripId
     * @param requestBody
     * @returns ApiResponseItineraryResponse OK
     * @throws ApiError
     */
    public static generateItinerary(
        tripId: number,
        requestBody: GenerateItineraryRequest,
    ): CancelablePromise<ApiResponseItineraryResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/ai/trips/{tripId}/itinerary/generate',
            path: {
                'tripId': tripId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
    /**
     * @param requestBody
     * @returns ApiResponseItineraryResponse OK
     * @throws ApiError
     */
    public static generateSelectedItinerary(
        requestBody: GenerateSelectedItineraryRequest,
    ): CancelablePromise<ApiResponseItineraryResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/ai/trips/itinerary/from-selection',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
}
