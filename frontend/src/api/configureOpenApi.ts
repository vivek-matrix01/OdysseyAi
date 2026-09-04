import { OpenAPI } from './generated';

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const stripTrailingApiPath = (url: string) => url.replace(/\/api\/?$/, '');

OpenAPI.BASE = stripTrailingApiPath(apiBaseUrl);
OpenAPI.TOKEN = async () => localStorage.getItem('token') || '';

