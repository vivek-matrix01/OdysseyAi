/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApiResponseWeatherResponse } from '../models/ApiResponseWeatherResponse';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class WeatherControllerService {
    /**
     * @param destination
     * @param startDate
     * @param endDate
     * @returns ApiResponseWeatherResponse OK
     * @throws ApiError
     */
    public static getWeather(
        destination: string,
        startDate: string,
        endDate: string,
    ): CancelablePromise<ApiResponseWeatherResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/travel/weather',
            query: {
                'destination': destination,
                'startDate': startDate,
                'endDate': endDate,
            },
        });
    }
}
