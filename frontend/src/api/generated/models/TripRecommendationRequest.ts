/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type TripRecommendationRequest = {
    tripType: string;
    destination: string;
    budget: number;
    currency: string;
    travelers: number;
    startDate: string;
    endDate: string;
    preferences?: string;
    includeHotels?: boolean;
};

