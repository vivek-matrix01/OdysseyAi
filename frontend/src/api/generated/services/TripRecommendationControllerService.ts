/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApiResponseTripRecommendationResponse } from '../models/ApiResponseTripRecommendationResponse';
import type { TripRecommendationRequest } from '../models/TripRecommendationRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class TripRecommendationControllerService {
    /**
     * @param requestBody
     * @returns ApiResponseTripRecommendationResponse OK
     * @throws ApiError
     */
    public static recommend(
        requestBody: TripRecommendationRequest,
    ): CancelablePromise<ApiResponseTripRecommendationResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/ai/recommendations',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
}
