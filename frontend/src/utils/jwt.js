/**
 * Parses JWT token without external libraries
 */
export function parseJwt(token) {
  if (!token) return null;
  try {
    const base64Url = token.split('.')[1];
    if (!base64Url) return null;
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error("Failed to parse JWT", e);
    return null;
  }
}

/**
 * Gets seconds until token expiry
 */
export function getTokenTimeRemaining(token) {
  const decoded = parseJwt(token);
  if (!decoded || !decoded.exp) return 0;
  const nowInSec = Math.floor(Date.now() / 1000);
  const remainingSec = decoded.exp - nowInSec;
  return Math.max(0, remainingSec);
}

/**
 * Format seconds to mm:ss
 */
export function formatDuration(seconds) {
  if (seconds <= 0) return '00:00';
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}
