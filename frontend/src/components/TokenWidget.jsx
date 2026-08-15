import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Key, ChevronUp, ChevronDown, Copy, Check, Clock, Shield, RefreshCw, Activity, Terminal } from 'lucide-react';
import { formatDuration } from '../utils/jwt';

export const TokenWidget = () => {
  const { accessToken, refreshToken, decodedToken, timeRemaining, refreshSession, lastApiLog } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const [copiedAccess, setCopiedAccess] = useState(false);
  const [copiedRefresh, setCopiedRefresh] = useState(false);

  const copyToken = (text, type) => {
    navigator.clipboard.writeText(text);
    if (type === 'access') {
      setCopiedAccess(true);
      setTimeout(() => setCopiedAccess(false), 2000);
    } else {
      setCopiedRefresh(true);
      setTimeout(() => setCopiedRefresh(false), 2000);
    }
  };

  return (
    <div className="fixed bottom-4 right-4 z-40 max-w-lg w-full px-2 sm:px-0">
      <div className="glass-panel rounded-2xl border border-indigo-500/30 shadow-2xl overflow-hidden bg-[#0d1322]/95 backdrop-blur-xl">
        
        {/* Widget Header Bar */}
        <div
          onClick={() => setIsOpen(!isOpen)}
          className="flex items-center justify-between px-4 py-3 cursor-pointer hover:bg-slate-800/50 transition border-b border-slate-800"
        >
          <div className="flex items-center space-x-3">
            <div className="p-1.5 rounded-lg bg-indigo-600/20 border border-indigo-500/30 text-indigo-400">
              <Key className="w-4 h-4" />
            </div>
            <div>
              <div className="text-xs font-bold text-white flex items-center space-x-2">
                <span>JWT Token & Activity Inspector</span>
                {accessToken && (
                  <span className="px-2 py-0.5 text-[10px] font-mono font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30 rounded">
                    {formatDuration(timeRemaining)}
                  </span>
                )}
              </div>
              <p className="text-[10px] text-slate-400">
                {lastApiLog ? `${lastApiLog.method} ${lastApiLog.rawUrl} (${lastApiLog.status})` : 'Live session state'}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2 text-slate-400">
            {isOpen ? <ChevronDown className="w-4 h-4" /> : <ChevronUp className="w-4 h-4" />}
          </div>
        </div>

        {/* Collapsible Content */}
        {isOpen && (
          <div className="p-4 space-y-4 text-xs">
            
            {/* Last API Log Call Summary */}
            {lastApiLog && (
              <div className="p-3 bg-slate-900/90 rounded-xl border border-slate-800 space-y-1">
                <div className="flex items-center justify-between text-[11px]">
                  <span className="text-slate-400 font-semibold flex items-center space-x-1">
                    <Activity className="w-3.5 h-3.5 text-indigo-400" />
                    <span>Last API Invocation</span>
                  </span>
                  <span className="font-mono text-slate-400">{lastApiLog.durationMs} ms</span>
                </div>
                <div className="flex items-center space-x-2 font-mono text-[11px]">
                  <span className="text-purple-400 font-bold">{lastApiLog.method}</span>
                  <span className="text-slate-300 truncate max-w-[240px]">{lastApiLog.rawUrl}</span>
                  <span className={`px-1.5 py-0.2 text-[10px] rounded font-bold ${
                    lastApiLog.status >= 200 && lastApiLog.status < 300
                      ? 'bg-emerald-500/20 text-emerald-300'
                      : 'bg-rose-500/20 text-rose-300'
                  }`}>
                    {lastApiLog.status}
                  </span>
                </div>
              </div>
            )}

            {/* Access Token Box */}
            <div>
              <div className="flex items-center justify-between mb-1">
                <span className="text-slate-400 font-semibold">Access Token (Bearer)</span>
                {accessToken && (
                  <button
                    onClick={() => copyToken(accessToken, 'access')}
                    className="text-indigo-400 hover:underline flex items-center space-x-1"
                  >
                    {copiedAccess ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                    <span>{copiedAccess ? 'Copied' : 'Copy'}</span>
                  </button>
                )}
              </div>
              <div className="p-2.5 bg-slate-950 rounded-xl border border-slate-800 font-mono text-[10px] text-slate-300 truncate">
                {accessToken || <span className="text-slate-600 italic">No access token in session</span>}
              </div>
            </div>

            {/* Refresh Token Box */}
            <div>
              <div className="flex items-center justify-between mb-1">
                <span className="text-slate-400 font-semibold">Refresh Token</span>
                {refreshToken && (
                  <button
                    onClick={() => copyToken(refreshToken, 'refresh')}
                    className="text-indigo-400 hover:underline flex items-center space-x-1"
                  >
                    {copiedRefresh ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                    <span>{copiedRefresh ? 'Copied' : 'Copy'}</span>
                  </button>
                )}
              </div>
              <div className="p-2.5 bg-slate-950 rounded-xl border border-slate-800 font-mono text-[10px] text-slate-300 truncate">
                {refreshToken || <span className="text-slate-600 italic">No refresh token in session</span>}
              </div>
            </div>

            {/* Decoded JWT Claims */}
            {decodedToken && (
              <div>
                <span className="text-slate-400 font-semibold block mb-1">Decoded JWT Payload Claims</span>
                <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 font-mono text-[11px] text-indigo-300">
                  <pre className="whitespace-pre-wrap">{JSON.stringify(decodedToken, null, 2)}</pre>
                </div>
              </div>
            )}

            {/* Actions */}
            <div className="pt-2 flex justify-between items-center border-t border-slate-800">
              <button
                onClick={refreshSession}
                disabled={!refreshToken}
                className="px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-[11px] transition flex items-center space-x-1.5 disabled:opacity-40"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Call Refresh Token API</span>
              </button>

              <span className="text-[10px] text-slate-500 font-mono">Port: 3000 | CORS OK</span>
            </div>

          </div>
        )}
      </div>
    </div>
  );
};
