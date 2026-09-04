/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type TripResponse = {
    id?: number;
    title?: string;
    description?: string;
    destination?: string;
    startDate?: string;
    endDate?: string;
    status?: TripResponse.status;
    createdAt?: string;
    updatedAt?: string;
};
export namespace TripResponse {
    export enum status {
        PLANNED = 'PLANNED',
        ONGOING = 'ONGOING',
        COMPLETED = 'COMPLETED',
        CANCELLED = 'CANCELLED',
    }
}

