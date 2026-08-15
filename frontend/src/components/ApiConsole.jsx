import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../api/client';
import { Play, Copy, Check, Terminal, Code2, Send, Sparkles, Clock, Globe, ArrowRight, ShieldCheck, RefreshCw } from 'lucide-react';

const ENDPOINTS_CATALOG = [
  {
    id: 'auth-login',
    category: 'Auth Controller (/api/auth)',
    name: 'POST /api/auth/login',
    method: 'POST',
    path: '/api/auth/login',
    requiresAuth: false,
    defaultBody: JSON.stringify({ email: 'admin@odysseyai.com', password: 'Password123!' }, null, 2),
    description: 'Authenticate user with email and password, returning JWT access & refresh tokens.',
  },
  {
    id: 'auth-register',
    category: 'Auth Controller (/api/auth)',
    name: 'POST /api/auth/register',
    method: 'POST',
    path: '/api/auth/register',
    requiresAuth: false,
    defaultBody: JSON.stringify({ firstName: 'John', lastName: 'Doe', email: 'john.doe@example.com', password: 'Password123!' }, null, 2),
    description: 'Registers a new user account in the system.',
  },
  {
    id: 'auth-refresh',
    category: 'Auth Controller (/api/auth)',
    name: 'POST /api/auth/refresh',
    method: 'POST',
    path: '/api/auth/refresh',
    requiresAuth: false,
    defaultBody: JSON.stringify({ refreshToken: 'PASTE_REFRESH_TOKEN_HERE' }, null, 2),
    description: 'Validates stored refresh token and returns a new access token.',
  },
  {
    id: 'auth-logout',
    category: 'Auth Controller (/api/auth)',
    name: 'POST /api/auth/logout',
    method: 'POST',
    path: '/api/auth/logout',
    requiresAuth: true,
    defaultBody: '',
    description: 'Revokes active user session and invalidates refresh tokens.',
  },
  {
    id: 'auth-verify-email',
    category: 'Auth Controller (/api/auth)',
    name: 'GET /api/auth/verify-email',
    method: 'GET',
    path: '/api/auth/verify-email?token=PASTE_TOKEN_HERE',
    requiresAuth: false,
    defaultBody: '',
    description: 'Verifies email address using the verification token sent via email.',
  },
  {
    id: 'auth-forgot-password',
    category: 'Auth Controller (/api/auth)',
    name: 'POST /api/auth/forgot-password',
    method: 'POST',
    path: '/api/auth/forgot-password',
    requiresAuth: false,
    defaultBody: JSON.stringify({ email: 'john.doe@example.com' }, null, 2),
    description: 'Initiates password reset flow by sending a reset token.',
  },
  {
    id: 'auth-reset-password',
    category: 'Auth Controller (/api/auth)',
    name: 'POST /api/auth/reset-password',
    method: 'POST',
    path: '/api/auth/reset-password',
    requiresAuth: false,
    defaultBody: JSON.stringify({ token: 'PASTE_RESET_TOKEN', newPassword: 'NewSecurePassword123!' }, null, 2),
    description: 'Completes password reset with valid token and new password.',
  },
  {
    id: 'user-me-get',
    category: 'User Controller (/api/users)',
    name: 'GET /api/users/me',
    method: 'GET',
    path: '/api/users/me',
    requiresAuth: true,
    defaultBody: '',
    description: 'Retrieves current logged-in user details.',
  },
  {
    id: 'user-me-put',
    category: 'User Controller (/api/users)',
    name: 'PUT /api/users/me',
    method: 'PUT',
    path: '/api/users/me',
    requiresAuth: true,
    defaultBody: JSON.stringify({ firstName: 'John Updated', lastName: 'Doe Updated' }, null, 2),
    description: 'Updates current user profile metadata.',
  },
  {
    id: 'user-me-change-password',
    category: 'User Controller (/api/users)',
    name: 'PUT /api/users/me/change-password',
    method: 'PUT',
    path: '/api/users/me/change-password',
    requiresAuth: true,
    defaultBody: JSON.stringify({ currentPassword: 'Password123!', newPassword: 'NewPassword456!' }, null, 2),
    description: 'Changes current user password.',
  },
  {
    id: 'admin-users-get-all',
    category: 'Admin Controller (/api/admin/users)',
    name: 'GET /api/admin/users',
    method: 'GET',
    path: '/api/admin/users',
    requiresAuth: true,
    defaultBody: '',
    description: 'Fetches all registered user accounts (Requires ADMIN authority).',
  },
  {
    id: 'admin-users-get-by-id',
    category: 'Admin Controller (/api/admin/users)',
    name: 'GET /api/admin/users/{id}',
    method: 'GET',
    path: '/api/admin/users/1',
    requiresAuth: true,
    defaultBody: '',
    description: 'Fetches specific user account details by ID.',
  },
  {
    id: 'admin-users-update-role',
    category: 'Admin Controller (/api/admin/users)',
    name: 'PUT /api/admin/users/{id}/role',
    method: 'PUT',
    path: '/api/admin/users/1/role',
    requiresAuth: true,
    defaultBody: JSON.stringify({ role: 'ADMIN' }, null, 2),
    description: 'Updates target user authorization role (USER or ADMIN).',
  },
  {
    id: 'admin-users-lock',
    category: 'Admin Controller (/api/admin/users)',
    name: 'PUT /api/admin/users/{id}/lock',
    method: 'PUT',
    path: '/api/admin/users/1/lock',
    requiresAuth: true,
    defaultBody: '',
    description: 'Manually locks and suspends user account.',
  },
  {
    id: 'admin-users-unlock',
    category: 'Admin Controller (/api/admin/users)',
    name: 'PUT /api/admin/users/{id}/unlock',
    method: 'PUT',
    path: '/api/admin/users/1/unlock',
    requiresAuth: true,
    defaultBody: '',
    description: 'Clears lock state and restores user account to active.',
  },
  {
    id: 'admin-users-delete',
    category: 'Admin Controller (/api/admin/users)',
    name: 'DELETE /api/admin/users/{id}',
    method: 'DELETE',
    path: '/api/admin/users/1',
    requiresAuth: true,
    defaultBody: '',
    description: 'Permanently purges target user account from database.',
  },
];

export const ApiConsole = () => {
  const { accessToken, refreshToken, setLastApiLog } = useAuth();
  
  const [selectedEndpoint, setSelectedEndpoint] = useState(ENDPOINTS_CATALOG[0]);
  const [customPath, setCustomPath] = useState(ENDPOINTS_CATALOG[0].path);
  const [requestBody, setRequestBody] = useState(ENDPOINTS_CATALOG[0].defaultBody);
  const [includeAuthHeader, setIncludeAuthHeader] = useState(true);

  const [loading, setLoading] = useState(false);
  const [responseLog, setResponseLog] = useState(null);
  const [copied, setCopied] = useState(false);

  // Auto-fill refresh token in refresh endpoint body if available
  useEffect(() => {
    setCustomPath(selectedEndpoint.path);
    if (selectedEndpoint.id === 'auth-refresh' && refreshToken) {
      setRequestBody(JSON.stringify({ refreshToken }, null, 2));
    } else {
      setRequestBody(selectedEndpoint.defaultBody);
    }
    setIncludeAuthHeader(selectedEndpoint.requiresAuth);
  }, [selectedEndpoint, refreshToken]);

  // Execute API Request
  const handleExecuteRequest = async () => {
    setLoading(true);
    setResponseLog(null);

    let parsedBody = null;
    if (requestBody && selectedEndpoint.method !== 'GET' && selectedEndpoint.method !== 'HEAD') {
      try {
        parsedBody = JSON.parse(requestBody);
      } catch (err) {
        setLoading(false);
        setResponseLog({
          ok: false,
          status: 400,
          statusText: 'JSON Syntax Error',
          data: { error: 'Invalid JSON request body format' },
          durationMs: 0,
        });
        return;
      }
    }

    const res = await apiRequest({
      endpoint: customPath,
      method: selectedEndpoint.method,
      body: parsedBody,
      token: includeAuthHeader ? accessToken : null,
    });

    setLastApiLog(res);
    setResponseLog(res);
    setLoading(false);
  };

  const copyResponseToClipboard = () => {
    if (!responseLog) return;
    navigator.clipboard.writeText(JSON.stringify(responseLog.data, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="max-w-7xl mx-auto py-8 px-4">
      {/* Header */}
      <div className="glass-panel rounded-2xl p-6 sm:p-8 mb-6 border border-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-3 mb-2">
            <Terminal className="w-7 h-7 text-purple-400" />
            <h2 className="text-2xl font-bold text-white">Interactive API Explorer Studio</h2>
          </div>
          <p className="text-xs text-slate-400">
            Swagger/Postman-style interactive testing tool to inspect, configure, and execute all 16 backend endpoints.
          </p>
        </div>
        <div className="flex items-center space-x-2 bg-slate-900/80 px-3 py-1.5 rounded-xl border border-slate-800 text-xs text-slate-300">
          <Globe className="w-4 h-4 text-emerald-400" />
          <span>Base Target:</span>
          <span className="font-mono text-emerald-400 font-bold">http://localhost:8080</span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        
        {/* LEFT COLUMN: Endpoint Catalog & Request Builder (7 cols) */}
        <div className="lg:col-span-7 space-y-6">
          
          {/* Endpoint Selector Dropdown */}
          <div className="glass-panel rounded-2xl p-6 border border-slate-800">
            <label className="block text-xs font-semibold text-slate-300 mb-2">
              Select Target API Endpoint ({ENDPOINTS_CATALOG.length} Endpoints Available)
            </label>
            <select
              className="w-full px-4 py-3 rounded-xl glass-input text-xs font-mono font-semibold"
              value={selectedEndpoint.id}
              onChange={(e) => {
                const found = ENDPOINTS_CATALOG.find((ep) => ep.id === e.target.value);
                if (found) setSelectedEndpoint(found);
              }}
            >
              {ENDPOINTS_CATALOG.map((ep) => (
                <option key={ep.id} value={ep.id}>
                  [{ep.method}] {ep.path} — {ep.description.slice(0, 45)}...
                </option>
              ))}
            </select>

            {/* Endpoint Metadata Pill */}
            <div className="mt-3 p-3 bg-slate-900/80 rounded-xl border border-slate-800 text-xs space-y-1">
              <div className="flex items-center justify-between">
                <span className="text-slate-400">{selectedEndpoint.category}</span>
                <span className={`px-2 py-0.5 text-[10px] font-bold rounded ${
                  selectedEndpoint.method === 'GET' ? 'bg-emerald-500/20 text-emerald-300' :
                  selectedEndpoint.method === 'POST' ? 'bg-indigo-500/20 text-indigo-300' :
                  selectedEndpoint.method === 'PUT' ? 'bg-amber-500/20 text-amber-300' :
                  'bg-rose-500/20 text-rose-300'
                }`}>
                  {selectedEndpoint.method}
                </span>
              </div>
              <p className="text-slate-300 text-[11px]">{selectedEndpoint.description}</p>
            </div>
          </div>

          {/* Request Path & Params Editor */}
          <div className="glass-panel rounded-2xl p-6 border border-slate-800 space-y-4">
            <h3 className="text-xs font-bold text-white uppercase tracking-wider">Request Configuration</h3>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Target Endpoint Path</label>
              <div className="flex items-center space-x-2">
                <span className="px-3 py-2 bg-slate-900 text-slate-400 font-mono text-xs rounded-xl border border-slate-800">
                  {selectedEndpoint.method}
                </span>
                <input
                  type="text"
                  className="flex-1 px-4 py-2 rounded-xl glass-input text-xs font-mono"
                  value={customPath}
                  onChange={(e) => setCustomPath(e.target.value)}
                />
              </div>
            </div>

            {/* Auth Bearer Toggle */}
            <div className="flex items-center justify-between pt-2 border-t border-slate-800/80">
              <div className="flex items-center space-x-2">
                <ShieldCheck className={`w-4 h-4 ${includeAuthHeader ? 'text-emerald-400' : 'text-slate-500'}`} />
                <span className="text-xs text-slate-300 font-medium">Inject Bearer JWT Token Header</span>
              </div>
              <input
                type="checkbox"
                className="w-4 h-4 rounded text-indigo-600 bg-slate-900 border-slate-700"
                checked={includeAuthHeader}
                onChange={(e) => setIncludeAuthHeader(e.target.checked)}
              />
            </div>
          </div>

          {/* JSON Body Editor */}
          {selectedEndpoint.method !== 'GET' && selectedEndpoint.method !== 'DELETE' && (
            <div className="glass-panel rounded-2xl p-6 border border-slate-800 space-y-3">
              <div className="flex items-center justify-between">
                <label className="text-xs font-bold text-white uppercase tracking-wider">
                  Request JSON Body Payload
                </label>
                <button
                  onClick={() => setRequestBody(selectedEndpoint.defaultBody)}
                  className="text-[11px] text-indigo-400 hover:underline"
                >
                  Reset Template
                </button>
              </div>
              <textarea
                rows={7}
                className="w-full p-4 rounded-xl glass-input font-mono text-xs text-indigo-200 resize-y"
                value={requestBody}
                onChange={(e) => setRequestBody(e.target.value)}
              />
            </div>
          )}

          {/* Execute Button */}
          <button
            onClick={handleExecuteRequest}
            disabled={loading}
            className="w-full py-3.5 px-6 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 hover:from-indigo-500 hover:to-pink-500 text-white font-bold text-xs shadow-xl shadow-indigo-600/30 flex items-center justify-center space-x-2 transition disabled:opacity-50"
          >
            {loading ? (
              <RefreshCw className="w-5 h-5 animate-spin" />
            ) : (
              <>
                <Play className="w-4 h-4 fill-white" />
                <span>Execute API Call ({selectedEndpoint.method} {customPath})</span>
              </>
            )}
          </button>

        </div>

        {/* RIGHT COLUMN: Response Inspector & Live Payload (5 cols) */}
        <div className="lg:col-span-5 space-y-6">
          <div className="glass-panel rounded-2xl p-6 border border-slate-800 min-h-[500px] flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between border-b border-slate-800 pb-4 mb-4">
                <h3 className="text-xs font-bold text-white uppercase tracking-wider flex items-center space-x-2">
                  <Code2 className="w-4 h-4 text-purple-400" />
                  <span>Response Payload Inspector</span>
                </h3>

                {responseLog && (
                  <button
                    onClick={copyResponseToClipboard}
                    className="flex items-center space-x-1 px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold transition"
                  >
                    {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copied ? 'Copied!' : 'Copy JSON'}</span>
                  </button>
                )}
              </div>

              {!responseLog ? (
                <div className="py-24 text-center text-slate-500 space-y-3">
                  <Terminal className="w-12 h-12 mx-auto text-slate-700 animate-pulse" />
                  <p className="text-xs">Click "Execute API Call" to send request and inspect live HTTP response.</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {/* Status Bar */}
                  <div className="flex items-center justify-between p-3 rounded-xl bg-slate-900/90 border border-slate-800">
                    <div className="flex items-center space-x-2">
                      <span className="text-xs text-slate-400 font-medium">Status:</span>
                      <span
                        className={`px-2.5 py-1 rounded-md text-xs font-mono font-bold ${
                          responseLog.status >= 200 && responseLog.status < 300
                            ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                            : 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
                        }`}
                      >
                        {responseLog.status} {responseLog.statusText}
                      </span>
                    </div>

                    <div className="flex items-center space-x-1 text-xs text-slate-400 font-mono">
                      <Clock className="w-3.5 h-3.5 text-indigo-400" />
                      <span>{responseLog.durationMs} ms</span>
                    </div>
                  </div>

                  {/* Body JSON Tree */}
                  <div>
                    <label className="block text-[11px] font-semibold text-slate-400 mb-1.5">Response Data JSON</label>
                    <div className="p-4 rounded-xl bg-[#070a12] border border-slate-800 font-mono text-xs text-emerald-300 overflow-x-auto max-h-[400px]">
                      <pre className="whitespace-pre-wrap leading-relaxed">
                        {JSON.stringify(responseLog.data, null, 2)}
                      </pre>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};
