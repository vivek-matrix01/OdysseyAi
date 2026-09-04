/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ItineraryDayResponse } from './ItineraryDayResponse';
import type { RouteData } from './RouteData';
export type ItineraryResponse = {
    id?: number;
    tripId?: number;
    summary?: string;
    status?: ItineraryResponse.status;
    days?: Array<ItineraryDayResponse>;
    routes?: Array<RouteData>;
    estimatedHotelCost?: number;
    estimatedActivityCost?: number;
    estimatedTransportCost?: number;
    estimatedTotalCost?: number;
    localCurrency?: string;
    localCurrencySymbol?: string;
    exchangeRateFromInr?: number;
};
export namespace ItineraryResponse {
    export enum status {
        DRAFT = 'DRAFT',
        GENERATED = 'GENERATED',
        MODIFIED = 'MODIFIED',
    }
}

