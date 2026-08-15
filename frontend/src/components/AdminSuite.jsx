import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../api/client';
import { ShieldCheck, ShieldAlert, Lock, Unlock, Trash2, Eye, RefreshCw, AlertTriangle, CheckCircle2, User, ChevronRight, X, Shield, Search } from 'lucide-react';

export const AdminSuite = () => {
  const { user, accessToken, setLastApiLog } = useAuth();

  const [usersList, setUsersList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  // Selected User Modal details
  const [selectedUserDetail, setSelectedUserDetail] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);

  // Role Edit Modal
  const [roleModalUser, setRoleModalUser] = useState(null);
  const [targetRole, setTargetRole] = useState('USER');

  // Fetch all users (`GET /api/admin/users`)
  const fetchAllUsers = useCallback(async () => {
    if (!accessToken) return;
    setLoading(true);
    setErrorMsg(null);
    const res = await apiRequest({
      endpoint: '/api/admin/users',
      method: 'GET',
      token: accessToken,
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok && res.data?.data) {
      setUsersList(res.data.data);
    } else {
      setErrorMsg(res.data?.message || res.data?.error || 'Failed to fetch user list. (Requires ADMIN authority)');
    }
  }, [accessToken, setLastApiLog]);

  useEffect(() => {
    fetchAllUsers();
  }, [fetchAllUsers]);

  // Fetch single user detail (`GET /api/admin/users/{id}`)
  const handleInspectUser = async (id) => {
    setDetailLoading(true);
    const res = await apiRequest({
      endpoint: `/api/admin/users/${id}`,
      method: 'GET',
      token: accessToken,
    });
    setLastApiLog(res);
    setDetailLoading(false);

    if (res.ok && res.data?.data) {
      setSelectedUserDetail(res.data.data);
    } else {
      setErrorMsg(res.data?.message || 'Failed to fetch user details');
    }
  };

  // Change user role (`PUT /api/admin/users/{id}/role`)
  const handleSaveRole = async (e) => {
    e.preventDefault();
    if (!roleModalUser) return;
    setLoading(true);
    const res = await apiRequest({
      endpoint: `/api/admin/users/${roleModalUser.id}/role`,
      method: 'PUT',
      token: accessToken,
      body: { role: targetRole },
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok) {
      setSuccessMsg(`Role updated to ${targetRole} for User #${roleModalUser.id}`);
      setRoleModalUser(null);
      await fetchAllUsers();
    } else {
      setErrorMsg(res.data?.message || 'Failed to update user role');
    }
  };

  // Lock account (`PUT /api/admin/users/{id}/lock`)
  const handleLockUser = async (id) => {
    if (!window.confirm(`Are you sure you want to lock and suspend User #${id}?`)) return;
    setLoading(true);
    const res = await apiRequest({
      endpoint: `/api/admin/users/${id}/lock`,
      method: 'PUT',
      token: accessToken,
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok) {
      setSuccessMsg(`User #${id} account locked successfully.`);
      await fetchAllUsers();
    } else {
      setErrorMsg(res.data?.message || 'Failed to lock user');
    }
  };

  // Unlock account (`PUT /api/admin/users/{id}/unlock`)
  const handleUnlockUser = async (id) => {
    setLoading(true);
    const res = await apiRequest({
      endpoint: `/api/admin/users/${id}/unlock`,
      method: 'PUT',
      token: accessToken,
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok) {
      setSuccessMsg(`User #${id} account unlocked and restored.`);
      await fetchAllUsers();
    } else {
      setErrorMsg(res.data?.message || 'Failed to unlock user');
    }
  };

  // Delete account (`DELETE /api/admin/users/{id}`)
  const handleDeleteUser = async (id) => {
    if (!window.confirm(`PERMANENT ACTION: Are you sure you want to delete User #${id}?`)) return;
    setLoading(true);
    const res = await apiRequest({
      endpoint: `/api/admin/users/${id}`,
      method: 'DELETE',
      token: accessToken,
    });
    setLastApiLog(res);
    setLoading(false);

    if (res.ok) {
      setSuccessMsg(`User #${id} permanently purged from system database.`);
      await fetchAllUsers();
    } else {
      setErrorMsg(res.data?.message || 'Failed to delete user');
    }
  };

  const filteredUsers = usersList.filter((u) => {
    const q = searchQuery.toLowerCase();
    return (
      (u.firstName && u.firstName.toLowerCase().includes(q)) ||
      (u.lastName && u.lastName.toLowerCase().includes(q)) ||
      (u.email && u.email.toLowerCase().includes(q)) ||
      (u.id && String(u.id).includes(q))
    );
  });

  return (
    <div className="max-w-6xl mx-auto py-8 px-4">
      {/* Header Banner */}
      <div className="glass-panel rounded-2xl p-6 sm:p-8 mb-6 border border-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 mb-2">
            <ShieldCheck className="w-7 h-7 text-purple-400" />
            <h2 className="text-2xl font-bold text-white">Admin Command Center</h2>
            <span className="px-2.5 py-0.5 text-xs font-bold bg-purple-500/20 text-purple-300 border border-purple-500/30 rounded-full">
              `/api/admin/users`
            </span>
          </div>
          <p className="text-xs text-slate-400">
            System administrative suite to inspect, update roles, lock/unlock, and manage all accounts.
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={fetchAllUsers}
            disabled={loading}
            className="flex items-center space-x-2 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs transition shadow-lg shadow-indigo-600/30 disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            <span>Reload Directory</span>
          </button>
        </div>
      </div>

      {/* Alert Notices */}
      {errorMsg && (
        <div className="mb-6 p-4 rounded-xl text-xs bg-rose-500/10 border border-rose-500/30 text-rose-300 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <ShieldAlert className="w-5 h-5 text-rose-400 flex-shrink-0" />
            <span className="font-semibold">{errorMsg}</span>
          </div>
          <button onClick={() => setErrorMsg(null)} className="text-rose-400 hover:text-white">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {successMsg && (
        <div className="mb-6 p-4 rounded-xl text-xs bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0" />
            <span className="font-semibold">{successMsg}</span>
          </div>
          <button onClick={() => setSuccessMsg(null)} className="text-emerald-400 hover:text-white">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Search Bar */}
      <div className="mb-6 flex items-center">
        <div className="relative w-full max-w-md">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
          <input
            type="text"
            placeholder="Search by ID, Name, or Email..."
            className="w-full pl-10 pr-4 py-2.5 rounded-xl glass-input text-xs"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
      </div>

      {/* User Directory Table */}
      <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden shadow-2xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-slate-900/80 border-b border-slate-800 text-slate-400 font-semibold uppercase tracking-wider text-[11px]">
                <th className="py-3.5 px-4">ID</th>
                <th className="py-3.5 px-4">User</th>
                <th className="py-3.5 px-4">Role</th>
                <th className="py-3.5 px-4">Status / Lock</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading && usersList.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-slate-400">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-indigo-400" />
                    Fetching user account directory from `/api/admin/users`...
                  </td>
                </tr>
              ) : filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-slate-400">
                    No user accounts found. Ensure backend is running and active account has `ADMIN` role.
                  </td>
                </tr>
              ) : (
                filteredUsers.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-800/40 transition">
                    <td className="py-3.5 px-4 font-mono text-slate-400">#{u.id}</td>
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-slate-100">{u.firstName} {u.lastName}</div>
                      <div className="text-[11px] text-slate-400 font-mono">{u.email}</div>
                    </td>
                    <td className="py-3.5 px-4">
                      <span
                        className={`px-2.5 py-1 text-[11px] font-bold rounded-md uppercase border ${
                          u.role === 'ADMIN'
                            ? 'bg-purple-500/20 text-purple-300 border-purple-500/30'
                            : 'bg-indigo-500/20 text-indigo-300 border-indigo-500/30'
                        }`}
                      >
                        {u.role}
                      </span>
                    </td>
                    <td className="py-3.5 px-4">
                      {u.accountNonLocked === false || u.locked ? (
                        <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-md text-[11px] font-semibold bg-rose-500/20 text-rose-300 border border-rose-500/30">
                          <Lock className="w-3 h-3 mr-1" />
                          <span>Locked</span>
                        </span>
                      ) : (
                        <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-md text-[11px] font-semibold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                          <Unlock className="w-3 h-3 mr-1" />
                          <span>Active / Unlocked</span>
                        </span>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end space-x-2">
                        {/* View Detail (`GET /api/admin/users/{id}`) */}
                        <button
                          onClick={() => handleInspectUser(u.id)}
                          title="View Details"
                          className="p-1.5 rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white transition"
                        >
                          <Eye className="w-3.5 h-3.5" />
                        </button>

                        {/* Edit Role (`PUT /api/admin/users/{id}/role`) */}
                        <button
                          onClick={() => {
                            setRoleModalUser(u);
                            setTargetRole(u.role || 'USER');
                          }}
                          title="Change Role"
                          className="p-1.5 rounded-lg bg-purple-600/20 text-purple-300 hover:bg-purple-600/40 transition"
                        >
                          <Shield className="w-3.5 h-3.5" />
                        </button>

                        {/* Lock / Unlock toggle */}
                        {u.accountNonLocked === false || u.locked ? (
                          <button
                            onClick={() => handleUnlockUser(u.id)}
                            title="Unlock Account"
                            className="p-1.5 rounded-lg bg-emerald-600/20 text-emerald-300 hover:bg-emerald-600/40 transition"
                          >
                            <Unlock className="w-3.5 h-3.5" />
                          </button>
                        ) : (
                          <button
                            onClick={() => handleLockUser(u.id)}
                            title="Lock Account"
                            className="p-1.5 rounded-lg bg-amber-600/20 text-amber-300 hover:bg-amber-600/40 transition"
                          >
                            <Lock className="w-3.5 h-3.5" />
                          </button>
                        )}

                        {/* Delete User (`DELETE /api/admin/users/{id}`) */}
                        <button
                          onClick={() => handleDeleteUser(u.id)}
                          title="Delete User"
                          className="p-1.5 rounded-lg bg-rose-600/20 text-rose-300 hover:bg-rose-600/40 transition"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* --- MODAL 1: User Detail Inspector --- */}
      {selectedUserDetail && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="glass-panel w-full max-w-lg rounded-2xl p-6 border border-slate-700 shadow-2xl relative">
            <button
              onClick={() => setSelectedUserDetail(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-lg font-bold text-white mb-4 flex items-center space-x-2">
              <Eye className="w-5 h-5 text-indigo-400" />
              <span>User Detailed Inspector (`/api/admin/users/${selectedUserDetail.id}`)</span>
            </h3>

            <div className="space-y-3 text-xs">
              <div className="p-3 bg-slate-900/80 rounded-xl border border-slate-800 font-mono">
                <pre className="text-indigo-300 whitespace-pre-wrap">
                  {JSON.stringify(selectedUserDetail, null, 2)}
                </pre>
              </div>
            </div>

            <div className="mt-6 flex justify-end">
              <button
                onClick={() => setSelectedUserDetail(null)}
                className="px-4 py-2 rounded-xl bg-slate-800 text-slate-300 hover:bg-slate-700 text-xs font-semibold"
              >
                Close Inspector
              </button>
            </div>
          </div>
        </div>
      )}

      {/* --- MODAL 2: Change User Role --- */}
      {roleModalUser && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="glass-panel w-full max-w-md rounded-2xl p-6 border border-slate-700 shadow-2xl relative">
            <button
              onClick={() => setRoleModalUser(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-lg font-bold text-white mb-2 flex items-center space-x-2">
              <Shield className="w-5 h-5 text-purple-400" />
              <span>Update Authorization Role</span>
            </h3>

            <p className="text-xs text-slate-400 mb-4">
              Updating role for user <strong className="text-slate-200">{roleModalUser.firstName} {roleModalUser.lastName}</strong> (`#{roleModalUser.id}`)
            </p>

            <form onSubmit={handleSaveRole} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-2">Target Role</label>
                <select
                  className="w-full px-4 py-2.5 rounded-xl glass-input text-xs font-semibold"
                  value={targetRole}
                  onChange={(e) => setTargetRole(e.target.value)}
                >
                  <option value="USER">USER (Standard Role)</option>
                  <option value="ADMIN">ADMIN (Full Administrative Access)</option>
                </select>
              </div>

              <div className="pt-2 flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => setRoleModalUser(null)}
                  className="px-4 py-2 rounded-xl bg-slate-800 text-slate-300 hover:bg-slate-700 text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold shadow-lg shadow-purple-600/30"
                >
                  Save Role
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
