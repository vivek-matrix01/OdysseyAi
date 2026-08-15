import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../api/client';
import { User, Shield, Key, Edit3, CheckCircle2, AlertCircle, RefreshCw, Lock, Mail, Calendar, Sparkles } from 'lucide-react';
import { formatDuration } from '../utils/jwt';

export const UserProfile = () => {
  const { user, accessToken, timeRemaining, fetchProfile, refreshSession, logout, setLastApiLog } = useAuth();
  
  const [activeSubTab, setActiveSubTab] = useState('overview'); // overview, edit-profile, change-password
  const [loading, setLoading] = useState(false);
  const [feedback, setFeedback] = useState(null);

  // Form states
  const [profileForm, setProfileForm] = useState({ firstName: '', lastName: '' });
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '' });

  useEffect(() => {
    if (user) {
      setProfileForm({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
      });
    }
  }, [user]);

  // Handle Update Profile (`PUT /api/users/me`)
  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setFeedback(null);
    setLoading(true);
    const res = await apiRequest({
      endpoint: '/api/users/me',
      method: 'PUT',
      token: accessToken,
      body: profileForm,
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok) {
      await fetchProfile();
      setFeedback({
        type: 'success',
        message: res.data?.message || 'Profile metadata updated successfully.',
      });
    } else {
      setFeedback({
        type: 'error',
        message: res.data?.message || res.data?.error || 'Profile update failed.',
      });
    }
  };

  // Handle Change Password (`PUT /api/users/me/change-password`)
  const handleChangePassword = async (e) => {
    e.preventDefault();
    setFeedback(null);
    setLoading(true);
    const res = await apiRequest({
      endpoint: '/api/users/me/change-password',
      method: 'PUT',
      token: accessToken,
      body: passwordForm,
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok) {
      setPasswordForm({ currentPassword: '', newPassword: '' });
      setFeedback({
        type: 'success',
        message: res.data?.message || 'Password changed successfully.',
      });
    } else {
      setFeedback({
        type: 'error',
        message: res.data?.message || res.data?.error || 'Password change failed.',
      });
    }
  };

  if (!accessToken) {
    return (
      <div className="max-w-md mx-auto my-12 p-8 glass-panel rounded-2xl text-center border border-amber-500/30">
        <Shield className="w-12 h-12 text-amber-400 mx-auto mb-4 animate-bounce" />
        <h3 className="text-lg font-bold text-white mb-2">Authentication Required</h3>
        <p className="text-xs text-slate-400 mb-6">
          You must be logged in to view and manage your profile (`/api/users/me`).
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto py-8 px-4">
      {/* Top Banner / Avatar Header */}
      <div className="glass-panel rounded-2xl p-6 sm:p-8 mb-6 border border-slate-800 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none"></div>

        <div className="flex flex-col sm:flex-row items-center sm:items-start space-y-4 sm:space-y-0 sm:space-x-6">
          <div className="relative">
            <div className="w-20 h-20 rounded-2xl bg-gradient-to-tr from-indigo-500 via-purple-500 to-pink-500 p-1 shadow-xl">
              <div className="w-full h-full bg-slate-900 rounded-[14px] flex items-center justify-center font-extrabold text-2xl text-indigo-300">
                {user?.firstName ? user.firstName.charAt(0).toUpperCase() : 'U'}
              </div>
            </div>
            <span className={`absolute -bottom-1 -right-1 w-5 h-5 rounded-full border-2 border-slate-900 ${user?.enabled !== false ? 'bg-emerald-400' : 'bg-rose-500'}`} title={user?.enabled !== false ? 'Account Active' : 'Disabled'}></span>
          </div>

          <div className="flex-1 text-center sm:text-left">
            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2 mb-1">
              <h2 className="text-2xl font-bold text-white">
                {user ? `${user.firstName} ${user.lastName}` : 'Loading...'}
              </h2>
              <span className={`px-2.5 py-0.5 text-xs font-bold uppercase rounded-md ${
                user?.role === 'ADMIN'
                  ? 'bg-purple-500/20 text-purple-300 border border-purple-500/30'
                  : 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/30'
              }`}>
                {user?.role || 'USER'}
              </span>
              <span className={`px-2.5 py-0.5 text-xs font-semibold rounded-md ${
                user?.accountNonLocked !== false
                  ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
              }`}>
                {user?.accountNonLocked !== false ? 'Active' : 'Locked'}
              </span>
            </div>

            <p className="text-xs text-slate-400 font-mono mb-4">{user?.email}</p>

            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-4 text-xs text-slate-300">
              <div className="flex items-center space-x-1.5 bg-slate-900/60 px-3 py-1.5 rounded-lg border border-slate-800">
                <Lock className="w-3.5 h-3.5 text-amber-400" />
                <span>JWT Time Remaining:</span>
                <span className="font-mono font-bold text-amber-400">{formatDuration(timeRemaining)}</span>
              </div>
              <button
                onClick={refreshSession}
                className="flex items-center space-x-1.5 bg-indigo-600/20 hover:bg-indigo-600/30 text-indigo-300 px-3 py-1.5 rounded-lg border border-indigo-500/30 transition text-xs font-semibold"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Refresh Access Token</span>
              </button>
            </div>
          </div>
        </div>

        {/* Tab switchers */}
        <div className="flex items-center space-x-2 border-t border-slate-800/80 mt-6 pt-4">
          <button
            onClick={() => { setActiveSubTab('overview'); setFeedback(null); }}
            className={`px-4 py-2 text-xs font-semibold rounded-xl transition ${
              activeSubTab === 'overview'
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'text-slate-400 hover:bg-slate-800/60'
            }`}
          >
            Overview
          </button>
          <button
            onClick={() => { setActiveSubTab('edit-profile'); setFeedback(null); }}
            className={`px-4 py-2 text-xs font-semibold rounded-xl transition ${
              activeSubTab === 'edit-profile'
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'text-slate-400 hover:bg-slate-800/60'
            }`}
          >
            Edit Profile metadata (`PUT /api/users/me`)
          </button>
          <button
            onClick={() => { setActiveSubTab('change-password'); setFeedback(null); }}
            className={`px-4 py-2 text-xs font-semibold rounded-xl transition ${
              activeSubTab === 'change-password'
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'text-slate-400 hover:bg-slate-800/60'
            }`}
          >
            Change Password (`PUT /api/users/me/change-password`)
          </button>
        </div>
      </div>

      {/* Feedback Banner */}
      {feedback && (
        <div
          className={`mb-6 p-4 rounded-xl text-xs flex items-center space-x-3 border ${
            feedback.type === 'success'
              ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
              : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
          }`}
        >
          {feedback.type === 'success' ? (
            <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0" />
          ) : (
            <AlertCircle className="w-5 h-5 text-rose-400 flex-shrink-0" />
          )}
          <span className="font-semibold">{feedback.message}</span>
        </div>
      )}

      {/* --- OVERVIEW TAB --- */}
      {activeSubTab === 'overview' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="glass-panel rounded-2xl p-6 border border-slate-800">
            <h3 className="text-sm font-bold text-white mb-4 flex items-center space-x-2">
              <User className="w-4 h-4 text-indigo-400" />
              <span>User Profile Details (`/api/users/me`)</span>
            </h3>

            <dl className="space-y-3 text-xs">
              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <dt className="text-slate-400">User ID:</dt>
                <dd className="font-mono text-slate-200">{user?.id || 'N/A'}</dd>
              </div>
              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <dt className="text-slate-400">First Name:</dt>
                <dd className="font-semibold text-slate-200">{user?.firstName || 'N/A'}</dd>
              </div>
              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <dt className="text-slate-400">Last Name:</dt>
                <dd className="font-semibold text-slate-200">{user?.lastName || 'N/A'}</dd>
              </div>
              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <dt className="text-slate-400">Email Address:</dt>
                <dd className="font-mono text-indigo-300">{user?.email || 'N/A'}</dd>
              </div>
              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <dt className="text-slate-400">Role:</dt>
                <dd className="font-bold text-purple-400">{user?.role || 'USER'}</dd>
              </div>
            </dl>
          </div>

          <div className="glass-panel rounded-2xl p-6 border border-slate-800">
            <h3 className="text-sm font-bold text-white mb-4 flex items-center space-x-2">
              <Shield className="w-4 h-4 text-purple-400" />
              <span>Security & Account Flags</span>
            </h3>

            <dl className="space-y-3 text-xs">
              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <dt className="text-slate-400">Account Non-Locked:</dt>
                <dd className={`font-semibold ${user?.accountNonLocked !== false ? 'text-emerald-400' : 'text-rose-400'}`}>
                  {user?.accountNonLocked !== false ? 'True (Unlocked)' : 'False (Locked)'}
                </dd>
              </div>
              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <dt className="text-slate-400">Account Enabled:</dt>
                <dd className={`font-semibold ${user?.enabled !== false ? 'text-emerald-400' : 'text-rose-400'}`}>
                  {user?.enabled !== false ? 'True (Verified/Enabled)' : 'False (Disabled)'}
                </dd>
              </div>
              <div className="flex justify-between py-2 border-b border-slate-800/60">
                <dt className="text-slate-400">Credentials Non-Expired:</dt>
                <dd className="font-semibold text-emerald-400">True</dd>
              </div>
            </dl>
          </div>
        </div>
      )}

      {/* --- EDIT PROFILE TAB --- */}
      {activeSubTab === 'edit-profile' && (
        <div className="glass-panel rounded-2xl p-6 border border-slate-800">
          <h3 className="text-sm font-bold text-white mb-4 flex items-center space-x-2">
            <Edit3 className="w-4 h-4 text-indigo-400" />
            <span>Update Profile Details (`PUT /api/users/me`)</span>
          </h3>

          <form onSubmit={handleUpdateProfile} className="space-y-4 max-w-md">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">First Name</label>
              <input
                type="text"
                required
                className="w-full px-4 py-2.5 rounded-xl glass-input text-xs"
                value={profileForm.firstName}
                onChange={(e) => setProfileForm({ ...profileForm, firstName: e.target.value })}
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Last Name</label>
              <input
                type="text"
                required
                className="w-full px-4 py-2.5 rounded-xl glass-input text-xs"
                value={profileForm.lastName}
                onChange={(e) => setProfileForm({ ...profileForm, lastName: e.target.value })}
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="py-2.5 px-6 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs transition shadow-lg shadow-indigo-600/30 flex items-center space-x-2 disabled:opacity-50"
            >
              {loading ? <RefreshCw className="w-4 h-4 animate-spin" /> : <span>Save Profile Changes</span>}
            </button>
          </form>
        </div>
      )}

      {/* --- CHANGE PASSWORD TAB --- */}
      {activeSubTab === 'change-password' && (
        <div className="glass-panel rounded-2xl p-6 border border-slate-800">
          <h3 className="text-sm font-bold text-white mb-4 flex items-center space-x-2">
            <Key className="w-4 h-4 text-purple-400" />
            <span>Change Account Password (`PUT /api/users/me/change-password`)</span>
          </h3>

          <form onSubmit={handleChangePassword} className="space-y-4 max-w-md">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Current Password</label>
              <input
                type="password"
                required
                placeholder="••••••••"
                className="w-full px-4 py-2.5 rounded-xl glass-input text-xs"
                value={passwordForm.currentPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">New Password (min 6 chars)</label>
              <input
                type="password"
                required
                minLength={6}
                placeholder="••••••••"
                className="w-full px-4 py-2.5 rounded-xl glass-input text-xs"
                value={passwordForm.newPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="py-2.5 px-6 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white font-semibold text-xs transition shadow-lg shadow-purple-600/30 flex items-center space-x-2 disabled:opacity-50"
            >
              {loading ? <RefreshCw className="w-4 h-4 animate-spin" /> : <span>Update Password</span>}
            </button>
          </form>
        </div>
      )}
    </div>
  );
};
