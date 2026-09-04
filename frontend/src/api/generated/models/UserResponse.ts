/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type UserResponse = {
    firstName?: string;
    lastName?: string;
    email?: string;
    role?: UserResponse.role;
    id?: number;
};
export namespace UserResponse {
    export enum role {
        USER = 'USER',
        ADMIN = 'ADMIN',
    }
}

