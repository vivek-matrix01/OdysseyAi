/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BudgetAnalysis } from './BudgetAnalysis';
import type { RecommendedHotelResponse } from './RecommendedHotelResponse';
import type { RecommendedPlaceResponse } from './RecommendedPlaceResponse';
import type { RouteSegmentResponse } from './RouteSegmentResponse';
export type TripRecommendationResponse = {
    destination?: string;
    tripType?: string;
    budget?: number;
    currency?: string;
    travelers?: number;
    estimatedTotalCost?: number;
    estimatedTransportCost?: number;
    budgetAnalysis?: BudgetAnalysis;
    places?: Array<RecommendedPlaceResponse>;
    hotels?: Array<RecommendedHotelResponse>;
    recommendationReason?: string;
    totalDistanceKm?: number;
    petrolVehicleCost?: number;
    dieselVehicleCost?: number;
    routeSegments?: Array<RouteSegmentResponse>;
    potentialProblems?: Array<string>;
    localCurrency?: string;
    localCurrencySymbol?: string;
    exchangeRateFromInr?: number;
    localPetrolPricePerLiter?: number;
};

