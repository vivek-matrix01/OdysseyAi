/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApiResponseConfirmedTripResponse } from '../models/ApiResponseConfirmedTripResponse';
import type { ConfirmTripRecommendationRequest } from '../models/ConfirmTripRecommendationRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class TripRecommendationConfirmationControllerService {
    /**
     * @param requestBody
     * @returns ApiResponseConfirmedTripResponse OK
     * @throws ApiError
     */
    public static confirm(
        requestBody: ConfirmTripRecommendationRequest,
    ): CancelablePromise<ApiResponseConfirmedTripResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/ai/recommendations/confirm',
            body: requestBody,
            mediaType: 'application/json',
        });
    }
}
