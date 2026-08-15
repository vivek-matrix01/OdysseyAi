import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../api/client';
import { LogIn, UserPlus, KeyRound, CheckCircle2, AlertCircle, Mail, Lock, User, ArrowRight, RefreshCw, Send, ShieldAlert } from 'lucide-react';

export const AuthPortal = ({ onLoginSuccess }) => {
  const { login, setLastApiLog } = useAuth();
  const [subTab, setSubTab] = useState('login'); // login, register, forgot, reset, verify
  
  const [loading, setLoading] = useState(false);
  const [feedback, setFeedback] = useState(null); // { type: 'success'|'error', message, details }

  // Form states
  const [loginForm, setLoginForm] = useState({ email: '', password: '' });
  const [registerForm, setRegisterForm] = useState({ firstName: '', lastName: '', email: '', password: '' });
  const [forgotEmail, setForgotEmail] = useState('');
  const [resetForm, setResetForm] = useState({ token: '', newPassword: '' });
  const [verifyToken, setVerifyToken] = useState('');

  // Handle Login
  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setFeedback(null);
    setLoading(true);
    const res = await apiRequest({
      endpoint: '/api/auth/login',
      method: 'POST',
      body: loginForm,
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok && res.data?.data) {
      const { accessToken, refreshToken } = res.data.data;
      await login(accessToken, refreshToken);
      setFeedback({
        type: 'success',
        message: 'Login successful! JWT session initialized.',
      });
      if (onLoginSuccess) onLoginSuccess();
    } else {
      setFeedback({
        type: 'error',
        message: res.data?.message || res.data?.error || 'Authentication failed. Please check credentials.',
      });
    }
  };

  // Handle Register
  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setFeedback(null);
    setLoading(true);
    const res = await apiRequest({
      endpoint: '/api/auth/register',
      method: 'POST',
      body: registerForm,
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok) {
      setFeedback({
        type: 'success',
        message: res.data?.message || 'Registration successful! Verification email sent.',
        details: 'Check your mailbox or Mailtrap sandbox for the verification token.',
      });
      setSubTab('verify');
    } else {
      setFeedback({
        type: 'error',
        message: res.data?.message || res.data?.error || 'Registration failed.',
      });
    }
  };

  // Handle Forgot Password
  const handleForgotSubmit = async (e) => {
    e.preventDefault();
    setFeedback(null);
    setLoading(true);
    const res = await apiRequest({
      endpoint: '/api/auth/forgot-password',
      method: 'POST',
      body: { email: forgotEmail },
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok) {
      setFeedback({
        type: 'success',
        message: res.data?.message || 'Password reset link sent to registered email.',
      });
      setSubTab('reset');
    } else {
      setFeedback({
        type: 'error',
        message: res.data?.message || res.data?.error || 'Request failed.',
      });
    }
  };

  // Handle Reset Password
  const handleResetSubmit = async (e) => {
    e.preventDefault();
    setFeedback(null);
    setLoading(true);
    const res = await apiRequest({
      endpoint: '/api/auth/reset-password',
      method: 'POST',
      body: resetForm,
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok) {
      setFeedback({
        type: 'success',
        message: res.data?.message || 'Password reset successfully! You can now log in.',
      });
      setSubTab('login');
    } else {
      setFeedback({
        type: 'error',
        message: res.data?.message || res.data?.error || 'Password reset failed.',
      });
    }
  };

  // Handle Verify Email
  const handleVerifySubmit = async (e) => {
    e.preventDefault();
    setFeedback(null);
    setLoading(true);
    const res = await apiRequest({
      endpoint: `/api/auth/verify-email?token=${encodeURIComponent(verifyToken)}`,
      method: 'GET',
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok) {
      setFeedback({
        type: 'success',
        message: res.data?.message || 'Email verified successfully! Proceed to Login.',
      });
      setSubTab('login');
    } else {
      setFeedback({
        type: 'error',
        message: res.data?.message || res.data?.error || 'Verification failed. Invalid or expired token.',
      });
    }
  };

  return (
    <div className="max-w-xl mx-auto py-8 px-4">
      {/* Outer Card */}
      <div className="glass-panel rounded-2xl p-6 sm:p-8 shadow-2xl border border-slate-800 relative overflow-hidden">
        {/* Glow decorative light */}
        <div className="absolute -top-24 -right-24 w-48 h-48 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none"></div>
        <div className="absolute -bottom-24 -left-24 w-48 h-48 bg-purple-500/20 rounded-full blur-3xl pointer-events-none"></div>

        {/* Tab Navigation */}
        <div className="flex items-center justify-between border-b border-slate-800/80 pb-4 mb-6 gap-1 overflow-x-auto">
          <button
            onClick={() => { setSubTab('login'); setFeedback(null); }}
            className={`flex items-center space-x-1.5 px-3 py-2 text-xs font-semibold rounded-lg transition-all ${
              subTab === 'login'
                ? 'bg-indigo-600/30 text-indigo-300 border border-indigo-500/40 shadow-sm'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <LogIn className="w-3.5 h-3.5" />
            <span>Login</span>
          </button>

          <button
            onClick={() => { setSubTab('register'); setFeedback(null); }}
            className={`flex items-center space-x-1.5 px-3 py-2 text-xs font-semibold rounded-lg transition-all ${
              subTab === 'register'
                ? 'bg-indigo-600/30 text-indigo-300 border border-indigo-500/40 shadow-sm'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <UserPlus className="w-3.5 h-3.5" />
            <span>Register</span>
          </button>

          <button
            onClick={() => { setSubTab('verify'); setFeedback(null); }}
            className={`flex items-center space-x-1.5 px-3 py-2 text-xs font-semibold rounded-lg transition-all ${
              subTab === 'verify'
                ? 'bg-indigo-600/30 text-indigo-300 border border-indigo-500/40 shadow-sm'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Verify Email</span>
          </button>

          <button
            onClick={() => { setSubTab('forgot'); setFeedback(null); }}
            className={`flex items-center space-x-1.5 px-3 py-2 text-xs font-semibold rounded-lg transition-all ${
              subTab === 'forgot' || subTab === 'reset'
                ? 'bg-indigo-600/30 text-indigo-300 border border-indigo-500/40 shadow-sm'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <KeyRound className="w-3.5 h-3.5" />
            <span>Forgot Password</span>
          </button>
        </div>

        {/* Feedback Alert */}
        {feedback && (
          <div
            className={`mb-6 p-4 rounded-xl text-xs flex items-start space-x-3 border ${
              feedback.type === 'success'
                ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
                : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
            }`}
          >
            {feedback.type === 'success' ? (
              <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0 mt-0.5" />
            ) : (
              <AlertCircle className="w-5 h-5 text-rose-400 flex-shrink-0 mt-0.5" />
            )}
            <div>
              <div className="font-semibold">{feedback.message}</div>
              {feedback.details && <div className="mt-1 text-slate-400 font-mono">{feedback.details}</div>}
            </div>
          </div>
        )}

        {/* --- 1. LOGIN FORM --- */}
        {subTab === 'login' && (
          <form onSubmit={handleLoginSubmit} className="space-y-4">
            <div>
              <h2 className="text-xl font-bold text-white mb-1">Welcome Back</h2>
              <p className="text-xs text-slate-400">Sign in to obtain JWT access & refresh tokens.</p>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Email Address</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="email"
                  required
                  placeholder="admin@odysseyai.com or user@example.com"
                  className="w-full pl-9 pr-4 py-2.5 rounded-xl glass-input text-xs"
                  value={loginForm.email}
                  onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })}
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="password"
                  required
                  placeholder="••••••••"
                  className="w-full pl-9 pr-4 py-2.5 rounded-xl glass-input text-xs"
                  value={loginForm.password}
                  onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white font-semibold text-xs transition shadow-lg shadow-indigo-600/30 flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              {loading ? (
                <RefreshCw className="w-4 h-4 animate-spin text-white" />
              ) : (
                <>
                  <span>Sign In</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>
        )}

        {/* --- 2. REGISTER FORM --- */}
        {subTab === 'register' && (
          <form onSubmit={handleRegisterSubmit} className="space-y-4">
            <div>
              <h2 className="text-xl font-bold text-white mb-1">Create Account</h2>
              <p className="text-xs text-slate-400">Registers a new account (`POST /api/auth/register`)</p>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">First Name</label>
                <div className="relative">
                  <User className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                  <input
                    type="text"
                    required
                    placeholder="Alex"
                    className="w-full pl-9 pr-3 py-2.5 rounded-xl glass-input text-xs"
                    value={registerForm.firstName}
                    onChange={(e) => setRegisterForm({ ...registerForm, firstName: e.target.value })}
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">Last Name</label>
                <input
                  type="text"
                  required
                  placeholder="Morgan"
                  className="w-full px-3 py-2.5 rounded-xl glass-input text-xs"
                  value={registerForm.lastName}
                  onChange={(e) => setRegisterForm({ ...registerForm, lastName: e.target.value })}
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Email Address</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="email"
                  required
                  placeholder="alex.m@example.com"
                  className="w-full pl-9 pr-4 py-2.5 rounded-xl glass-input text-xs"
                  value={registerForm.email}
                  onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })}
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Password (min 8 chars)</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="password"
                  required
                  minLength={8}
                  placeholder="••••••••"
                  className="w-full pl-9 pr-4 py-2.5 rounded-xl glass-input text-xs"
                  value={registerForm.password}
                  onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white font-semibold text-xs transition shadow-lg shadow-indigo-600/30 flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              {loading ? (
                <RefreshCw className="w-4 h-4 animate-spin text-white" />
              ) : (
                <>
                  <span>Register Account</span>
                  <UserPlus className="w-4 h-4" />
                </>
              )}
            </button>
          </form>
        )}

        {/* --- 3. VERIFY EMAIL FORM --- */}
        {subTab === 'verify' && (
          <form onSubmit={handleVerifySubmit} className="space-y-4">
            <div>
              <h2 className="text-xl font-bold text-white mb-1">Verify Email Address</h2>
              <p className="text-xs text-slate-400">Calls `GET /api/auth/verify-email?token=...`</p>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Verification Token</label>
              <input
                type="text"
                required
                placeholder="Paste verification token received via email"
                className="w-full px-4 py-2.5 rounded-xl glass-input text-xs font-mono"
                value={verifyToken}
                onChange={(e) => setVerifyToken(e.target.value)}
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs transition shadow-lg shadow-emerald-600/30 flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              {loading ? (
                <RefreshCw className="w-4 h-4 animate-spin text-white" />
              ) : (
                <>
                  <span>Submit Email Verification</span>
                  <CheckCircle2 className="w-4 h-4" />
                </>
              )}
            </button>
          </form>
        )}

        {/* --- 4. FORGOT PASSWORD FORM --- */}
        {subTab === 'forgot' && (
          <form onSubmit={handleForgotSubmit} className="space-y-4">
            <div>
              <h2 className="text-xl font-bold text-white mb-1">Initiate Password Reset</h2>
              <p className="text-xs text-slate-400">Calls `POST /api/auth/forgot-password`</p>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Registered Email</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="email"
                  required
                  placeholder="user@example.com"
                  className="w-full pl-9 pr-4 py-2.5 rounded-xl glass-input text-xs"
                  value={forgotEmail}
                  onChange={(e) => setForgotEmail(e.target.value)}
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs transition shadow-lg shadow-indigo-600/30 flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              {loading ? (
                <RefreshCw className="w-4 h-4 animate-spin text-white" />
              ) : (
                <>
                  <span>Send Reset Link</span>
                  <Send className="w-4 h-4" />
                </>
              )}
            </button>

            <div className="text-center pt-2">
              <button
                type="button"
                onClick={() => setSubTab('reset')}
                className="text-xs text-indigo-400 hover:underline"
              >
                Already have a reset token? Enter it here →
              </button>
            </div>
          </form>
        )}

        {/* --- 5. RESET PASSWORD FORM --- */}
        {subTab === 'reset' && (
          <form onSubmit={handleResetSubmit} className="space-y-4">
            <div>
              <h2 className="text-xl font-bold text-white mb-1">Reset Password</h2>
              <p className="text-xs text-slate-400">Calls `POST /api/auth/reset-password`</p>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Reset Token</label>
              <input
                type="text"
                required
                placeholder="Paste token from email"
                className="w-full px-4 py-2.5 rounded-xl glass-input text-xs font-mono"
                value={resetForm.token}
                onChange={(e) => setResetForm({ ...resetForm, token: e.target.value })}
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">New Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="password"
                  required
                  minLength={6}
                  placeholder="••••••••"
                  className="w-full pl-9 pr-4 py-2.5 rounded-xl glass-input text-xs"
                  value={resetForm.newPassword}
                  onChange={(e) => setResetForm({ ...resetForm, newPassword: e.target.value })}
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white font-semibold text-xs transition shadow-lg shadow-purple-600/30 flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              {loading ? (
                <RefreshCw className="w-4 h-4 animate-spin text-white" />
              ) : (
                <>
                  <span>Complete Password Reset</span>
                  <KeyRound className="w-4 h-4" />
                </>
              )}
            </button>
          </form>
        )}

      </div>
    </div>
  );
};
