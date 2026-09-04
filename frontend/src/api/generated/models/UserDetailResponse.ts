/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type UserDetailResponse = {
    id?: number;
    firstName?: string;
    lastName?: string;
    email?: string;
    role?: UserDetailResponse.role;
    accountStatus?: UserDetailResponse.accountStatus;
    emailVerified?: boolean;
    failedLoginAttempts?: number;
    accountLockedUntil?: string;
    createdAt?: string;
    updatedAt?: string;
};
export namespace UserDetailResponse {
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

