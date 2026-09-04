/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { WeatherData } from './WeatherData';
export type WeatherResponse = {
    destination?: string;
    startDate?: string;
    endDate?: string;
    forecastAvailable?: boolean;
    dailyForecast?: Array<WeatherData>;
    message?: string;
};

