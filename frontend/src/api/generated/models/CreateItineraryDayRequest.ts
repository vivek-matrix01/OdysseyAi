/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateActivityRequest } from './CreateActivityRequest';
export type CreateItineraryDayRequest = {
    dayNumber: number;
    date: string;
    notes?: string;
    activities?: Array<CreateActivityRequest>;
};

