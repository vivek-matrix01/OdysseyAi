/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApiResponseTripCostEstimate } from '../models/ApiResponseTripCostEstimate';
import type { TripCostEstimateRequest } from '../models/TripCostEstimateRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class TripCostEstimateControllerService {
    /**
     * @param tripId
     * @param requestBody
     * @returns ApiResponseTripCostEstimate OK
     * @throws ApiError
     */
    public static estimateCost(
        tripId: number,
        requestBody: TripCostEstimateRequest,
    ): CancelablePromise<ApiResponseTripCostEstimate> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/trips/{tripId}/cost-estimate',
            path: {
                'tripId': tripId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }
}
