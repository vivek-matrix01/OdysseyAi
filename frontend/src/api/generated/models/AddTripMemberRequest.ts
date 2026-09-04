/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type AddTripMemberRequest = {
    email: string;
    role: AddTripMemberRequest.role;
};
export namespace AddTripMemberRequest {
    export enum role {
        VIEWER = 'VIEWER',
        EDITOR = 'EDITOR',
    }
}

