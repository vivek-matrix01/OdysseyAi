/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type UserSummaryResponse = {
    id?: number;
    firstName?: string;
    lastName?: string;
    email?: string;
    role?: UserSummaryResponse.role;
    accountStatus?: UserSummaryResponse.accountStatus;
};
export namespace UserSummaryResponse {
    export enum role {
        USER = 'USER',
        ADMIN = 'ADMIN',
    }
    export enum accountStatus {
        ACTIVE = 'ACTIVE',
        BLOCKED = 'BLOCKED',
        SUSPENDED = 'SUSPENDED',
        PENDING_VERIFICATION = 'PENDING_VERIFICATION',
        DELETED = 'DELETED',
    }
}

