/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LocalTime } from './LocalTime';
export type ActivityResponse = {
    id?: number;
    title?: string;
    description?: string;
    startTime?: LocalTime;
    endTime?: LocalTime;
    location?: string;
    estimatedCost?: number;
    currency?: string;
    type?: ActivityResponse.type;
};
export namespace ActivityResponse {
    export enum type {
        SIGHTSEEING = 'SIGHTSEEING',
        FOOD = 'FOOD',
        HOTEL = 'HOTEL',
        TRANSPORT = 'TRANSPORT',
        SHOPPING = 'SHOPPING',
        ENTERTAINMENT = 'ENTERTAINMENT',
        OTHER = 'OTHER',
    }
}

