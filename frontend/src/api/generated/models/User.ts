/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { GrantedAuthority } from './GrantedAuthority';
export type User = {
    id?: number;
    createdAt?: string;
    updatedAt?: string;
    firstName: string;
    lastName: string;
    email?: string;
    profilePicture?: string;
    password?: string;
    role?: User.role;
    accountStatus?: User.accountStatus;
    emailVerified?: boolean;
    failedLoginAttempts?: number;
    accountLockedUntil?: string;
    enabled?: boolean;
    username?: string;
    authorities?: Array<GrantedAuthority>;
    accountNonLocked?: boolean;
    credentialsNonExpired?: boolean;
    accountNonExpired?: boolean;
};
export namespace User {
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

