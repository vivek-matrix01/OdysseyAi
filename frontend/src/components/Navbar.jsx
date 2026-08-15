import React from 'react';
import { useAuth } from '../context/AuthContext';
import { ShieldCheck, UserCheck, Key, Terminal, LayoutDashboard, LogOut, RefreshCw, Zap, Lock } from 'lucide-react';
import { formatDuration } from '../utils/jwt';

export const Navbar = ({ activeTab, setActiveTab }) => {
  const { user, accessToken, timeRemaining, refreshSession, logout, loading } = useAuth();

  return (
    <header className="sticky top-0 z-40 w-full glass-panel border-b border-slate-800 bg-[#090d16]/80 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Logo & Title */}
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-purple-600 to-pink-500 flex items-center justify-center shadow-lg shadow-indigo-500/20">
              <Zap className="w-6 h-6 text-white" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-extrabold text-xl tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white via-slate-200 to-indigo-300">
                  OdysseyAi
                </span>
                <span className="px-2 py-0.5 text-xs font-mono font-semibold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 rounded-full">
                  v1.0 API Tester
                </span>
              </div>
              <p className="text-xs text-slate-400 font-medium">Spring Boot Auth & Management Interface</p>
            </div>
          </div>

          {/* Center Nav Mode Switcher */}
          <div className="hidden md:flex items-center space-x-1 p-1 bg-slate-900/80 border border-slate-800 rounded-xl">
            <button
              onClick={() => setActiveTab('auth')}
              className={`flex items-center space-x-2 px-4 py-2 text-xs font-semibold rounded-lg transition-all ${
                activeTab === 'auth'
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Key className="w-4 h-4" />
              <span>Auth Portal</span>
            </button>

            <button
              onClick={() => setActiveTab('profile')}
              className={`flex items-center space-x-2 px-4 py-2 text-xs font-semibold rounded-lg transition-all ${
                activeTab === 'profile'
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <UserCheck className="w-4 h-4" />
              <span>User Profile</span>
            </button>

            <button
              onClick={() => setActiveTab('admin')}
              className={`flex items-center space-x-2 px-4 py-2 text-xs font-semibold rounded-lg transition-all ${
                activeTab === 'admin'
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <ShieldCheck className="w-4 h-4" />
              <span>Admin Console</span>
              {user?.role === 'ADMIN' && (
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              )}
            </button>

            <button
              onClick={() => setActiveTab('api-explorer')}
              className={`flex items-center space-x-2 px-4 py-2 text-xs font-semibold rounded-lg transition-all ${
                activeTab === 'api-explorer'
                  ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-md shadow-purple-600/30'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Terminal className="w-4 h-4 text-purple-300" />
              <span>API Explorer Studio</span>
            </button>
          </div>

          {/* Right Session Status & User Pill */}
          <div className="flex items-center space-x-3">
            {accessToken ? (
              <div className="flex items-center space-x-3 bg-slate-900/90 border border-slate-800/80 rounded-xl px-3 py-1.5">
                {/* Token timer */}
                <div className="hidden sm:flex items-center space-x-1.5 text-xs font-mono px-2 py-1 rounded-md bg-slate-800/60 text-slate-300 border border-slate-700/50">
                  <Lock className="w-3.5 h-3.5 text-amber-400" />
                  <span>{formatDuration(timeRemaining)}</span>
                </div>

                {/* User info */}
                <div className="text-right">
                  <div className="text-xs font-bold text-slate-200 flex items-center justify-end space-x-1">
                    <span>{user ? `${user.firstName} ${user.lastName}` : 'Authenticated'}</span>
                    <span className={`px-1.5 py-0.2 text-[10px] uppercase font-bold rounded ${
                      user?.role === 'ADMIN' 
                        ? 'bg-purple-500/20 text-purple-300 border border-purple-500/30' 
                        : 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                    }`}>
                      {user?.role || 'USER'}
                    </span>
                  </div>
                  <div className="text-[10px] text-slate-400 truncate max-w-[120px]">
                    {user?.email || 'Active JWT Session'}
                  </div>
                </div>

                <button
                  onClick={refreshSession}
                  disabled={loading}
                  title="Refresh Access Token"
                  className="p-1.5 text-slate-400 hover:text-indigo-400 hover:bg-slate-800 rounded-lg transition"
                >
                  <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin text-indigo-400' : ''}`} />
                </button>

                <button
                  onClick={logout}
                  title="Sign Out"
                  className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-slate-800 rounded-lg transition"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <span className="inline-flex items-center px-2.5 py-1 text-xs font-medium rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">
                  <span className="w-1.5 h-1.5 rounded-full bg-amber-400 mr-1.5 animate-ping"></span>
                  Not Logged In
                </span>
              </div>
            )}
          </div>

        </div>

        {/* Mobile Navigation Tabs */}
        <div className="md:hidden flex items-center justify-around py-2 border-t border-slate-800/60">
          <button
            onClick={() => setActiveTab('auth')}
            className={`p-2 rounded-lg text-xs font-semibold flex flex-col items-center ${activeTab === 'auth' ? 'text-indigo-400' : 'text-slate-400'}`}
          >
            <Key className="w-4 h-4 mb-0.5" />
            <span>Auth</span>
          </button>
          <button
            onClick={() => setActiveTab('profile')}
            className={`p-2 rounded-lg text-xs font-semibold flex flex-col items-center ${activeTab === 'profile' ? 'text-indigo-400' : 'text-slate-400'}`}
          >
            <UserCheck className="w-4 h-4 mb-0.5" />
            <span>Profile</span>
          </button>
          <button
            onClick={() => setActiveTab('admin')}
            className={`p-2 rounded-lg text-xs font-semibold flex flex-col items-center ${activeTab === 'admin' ? 'text-indigo-400' : 'text-slate-400'}`}
          >
            <ShieldCheck className="w-4 h-4 mb-0.5" />
            <span>Admin</span>
          </button>
          <button
            onClick={() => setActiveTab('api-explorer')}
            className={`p-2 rounded-lg text-xs font-semibold flex flex-col items-center ${activeTab === 'api-explorer' ? 'text-purple-400' : 'text-slate-400'}`}
          >
            <Terminal className="w-4 h-4 mb-0.5" />
            <span>Explorer</span>
          </button>
        </div>

      </div>
    </header>
  );
};
