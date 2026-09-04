/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { SelectedHotelRequest } from './SelectedHotelRequest';
export type GenerateSelectedItineraryRequest = {
    tripId: number;
    hotel?: SelectedHotelRequest;
    selectedPlaceIds: Array<string>;
    currency: string;
    additionalPreferences?: string;
};

