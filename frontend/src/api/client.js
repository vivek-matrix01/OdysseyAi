const BASE_URL = 'http://localhost:8080';

/**
 * Custom API client wrapper supporting latency tracking and detailed response parsing.
 */
export async function apiRequest({
  endpoint,
  method = 'GET',
  body = null,
  headers = {},
  token = null,
}) {
  const startTime = performance.now();
  const url = endpoint.startsWith('http') ? endpoint : `${BASE_URL}${endpoint}`;

  const defaultHeaders = {
    'Content-Type': 'application/json',
    ...headers,
  };

  if (token) {
    defaultHeaders['Authorization'] = `Bearer ${token}`;
  }

  const options = {
    method,
    headers: defaultHeaders,
  };

  if (body && method !== 'GET' && method !== 'HEAD') {
    options.body = typeof body === 'string' ? body : JSON.stringify(body);
  }

  try {
    const res = await fetch(url, options);
    const endTime = performance.now();
    const durationMs = Math.round(endTime - startTime);

    let data = null;
    const contentType = res.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      data = await res.json();
    } else {
      const text = await res.text();
      data = text ? { message: text } : null;
    }

    return {
      ok: res.ok,
      status: res.status,
      statusText: res.statusText,
      data,
      durationMs,
      headers: Object.fromEntries(res.headers.entries()),
      rawUrl: url,
      method,
    };
  } catch (error) {
    const endTime = performance.now();
    const durationMs = Math.round(endTime - startTime);

    return {
      ok: false,
      status: 0,
      statusText: 'Network Error',
      data: {
        error: error.message || 'Failed to connect to backend server at http://localhost:8080',
        hint: 'Ensure Spring Boot backend is running on port 8080',
      },
      durationMs,
      headers: {},
      rawUrl: url,
      method,
    };
  }
}
