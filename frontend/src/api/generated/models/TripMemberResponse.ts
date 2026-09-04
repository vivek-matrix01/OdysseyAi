/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type TripMemberResponse = {
    id?: number;
    userId?: number;
    firstName?: string;
    lastName?: string;
    email?: string;
    role?: TripMemberResponse.role;
};
export namespace TripMemberResponse {
    export enum role {
        VIEWER = 'VIEWER',
        EDITOR = 'EDITOR',
    }
}

