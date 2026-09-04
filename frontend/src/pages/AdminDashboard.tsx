import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { 
  Shield, 
  Users, 
  UserCheck, 
  UserX, 
  Search, 
  RotateCw, 
  Eye, 
  Lock, 
  Unlock, 
  Trash2, 
  ArrowLeft,
  X,
  Mail,
  AlertCircle,
  CheckCircle2,
  Clock,
  ShieldAlert,
  User as UserIcon
} from 'lucide-react';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import ConfirmModal from '../components/ui/ConfirmModal';
import { useAuth } from '../context/AuthContext';
import { AdminControllerService } from '../api/generated';
import type { UserSummaryResponse, UserDetailResponse, UpdateRoleRequest } from '../api/generated';

const AdminDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user: authUser } = useAuth();

  // Guard: Verify Admin Role
  useEffect(() => {
    if (authUser && authUser.role !== 'ADMIN') {
      toast.error('Access denied. Administrator privileges required.');
      navigate('/dashboard');
    }
  }, [authUser, navigate]);

  // Data states
  const [users, setUsers] = useState<UserSummaryResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [roleFilter, setRoleFilter] = useState<'ALL' | 'USER' | 'ADMIN'>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Details Modal state
  const [selectedUserDetail, setSelectedUserDetail] = useState<UserDetailResponse | null>(null);
  const [isDetailLoading, setIsDetailLoading] = useState<boolean>(false);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState<boolean>(false);

  // Confirmation Modal state
  const [confirmModal, setConfirmModal] = useState<{
    isOpen: boolean;
    title: string;
    message: string;
    confirmText?: string;
    variant?: 'danger' | 'warning' | 'primary';
    isLoading: boolean;
    onConfirm: () => void;
  }>({
    isOpen: false,
    title: '',
    message: '',
    confirmText: 'Confirm',
    variant: 'danger',
    isLoading: false,
    onConfirm: () => {}
  });

  const getErrorMessage = (err: any, fallback: string) => {
    return err?.body?.message || err?.response?.data?.message || err?.message || fallback;
  };

  // Fetch all users (GET /api/admin/users)
  const fetchUsers = async () => {
    setIsLoading(true);
    try {
      const response = await AdminControllerService.getAllUsers();
      if (response.data) {
        setUsers(response.data);
      }
    } catch (err: any) {
      toast.error(getErrorMessage(err, 'Failed to load user accounts.'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  // Filtered users calculation
  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const fullName = `${u.firstName || ''} ${u.lastName || ''}`.toLowerCase();
      const email = (u.email || '').toLowerCase();
      const q = searchQuery.toLowerCase().trim();

      const matchesSearch = !q || fullName.includes(q) || email.includes(q);
      const matchesRole = roleFilter === 'ALL' || u.role === roleFilter;
      const matchesStatus = statusFilter === 'ALL' || u.accountStatus === statusFilter;

      return matchesSearch && matchesRole && matchesStatus;
    });
  }, [users, searchQuery, roleFilter, statusFilter]);

  // Statistics calculation
  const stats = useMemo(() => {
    const total = users.length;
    const active = users.filter((u) => u.accountStatus === 'ACTIVE').length;
    const locked = users.filter((u) => u.accountStatus === 'BLOCKED' || u.accountStatus === 'SUSPENDED').length;
    const admins = users.filter((u) => u.role === 'ADMIN').length;
    return { total, active, locked, admins };
  }, [users]);

  // View User Detail (GET /api/admin/users/{id})
  const handleViewDetail = async (id: number) => {
    setIsDetailLoading(true);
    setIsDetailModalOpen(true);
    try {
      const response = await AdminControllerService.getUserById(id);
      if (response.data) {
        setSelectedUserDetail(response.data);
      }
    } catch (err: any) {
      toast.error(getErrorMessage(err, 'Failed to fetch user details.'));
      setIsDetailModalOpen(false);
    } finally {
      setIsDetailLoading(false);
    }
  };

  // Change Role (PUT /api/admin/users/{id}/role)
  const handleChangeRole = (userId: number, currentRole?: string) => {
    const newRole: UpdateRoleRequest.role = currentRole === 'ADMIN' ? 'USER' as any : 'ADMIN' as any;
    
    setConfirmModal({
      isOpen: true,
      title: `Change Role to ${newRole}`,
      message: `Are you sure you want to change this user's authorization role to ${newRole}?`,
      confirmText: 'Change Role',
      variant: 'primary',
      isLoading: false,
      onConfirm: async () => {
        setConfirmModal((prev) => ({ ...prev, isLoading: true }));
        try {
          await AdminControllerService.changeUserRole(userId, { role: newRole });
          toast.success(`User role updated to ${newRole}`);
          setConfirmModal((prev) => ({ ...prev, isOpen: false }));
          await fetchUsers();
          if (selectedUserDetail && selectedUserDetail.id === userId) {
            handleViewDetail(userId);
          }
        } catch (err: any) {
          toast.error(getErrorMessage(err, 'Failed to update user role.'));
        } finally {
          setConfirmModal((prev) => ({ ...prev, isLoading: false }));
        }
      }
    });
  };

  // Lock User (PUT /api/admin/users/{id}/lock)
  const handleLockUser = (userId: number, userName: string) => {
    setConfirmModal({
      isOpen: true,
      title: 'Lock User Account',
      message: `Are you sure you want to manually suspend and lock the account of "${userName}"? They will not be able to log in.`,
      confirmText: 'Lock Account',
      variant: 'danger',
      isLoading: false,
      onConfirm: async () => {
        setConfirmModal((prev) => ({ ...prev, isLoading: true }));
        try {
          await AdminControllerService.lockUserAccount(userId);
          toast.success('User account locked successfully.');
          setConfirmModal((prev) => ({ ...prev, isOpen: false }));
          await fetchUsers();
          if (selectedUserDetail && selectedUserDetail.id === userId) {
            handleViewDetail(userId);
          }
        } catch (err: any) {
          toast.error(getErrorMessage(err, 'Failed to lock user account.'));
        } finally {
          setConfirmModal((prev) => ({ ...prev, isLoading: false }));
        }
      }
    });
  };

  // Unlock User (PUT /api/admin/users/{id}/unlock)
  const handleUnlockUser = (userId: number, userName: string) => {
    setConfirmModal({
      isOpen: true,
      title: 'Unlock User Account',
      message: `Restore and clear tracking state for "${userName}"? The user will be able to log in normally.`,
      confirmText: 'Unlock Account',
      variant: 'primary',
      isLoading: false,
      onConfirm: async () => {
        setConfirmModal((prev) => ({ ...prev, isLoading: true }));
        try {
          await AdminControllerService.unlockUserAccount(userId);
          toast.success('User account unlocked and restored to active.');
          setConfirmModal((prev) => ({ ...prev, isOpen: false }));
          await fetchUsers();
          if (selectedUserDetail && selectedUserDetail.id === userId) {
            handleViewDetail(userId);
          }
        } catch (err: any) {
          toast.error(getErrorMessage(err, 'Failed to unlock user account.'));
        } finally {
          setConfirmModal((prev) => ({ ...prev, isLoading: false }));
        }
      }
    });
  };

  // Delete User (DELETE /api/admin/users/{id})
  const handleDeleteUser = (userId: number, userName: string) => {
    setConfirmModal({
      isOpen: true,
      title: 'Permanently Purge Account',
      message: `Are you sure you want to permanently delete user "${userName}"? This action is irreversible and all associated records will be purged.`,
      confirmText: 'Purge Account',
      variant: 'danger',
      isLoading: false,
      onConfirm: async () => {
        setConfirmModal((prev) => ({ ...prev, isLoading: true }));
        try {
          await AdminControllerService.deleteUserAccount(userId);
          toast.success('User account permanently deleted.');
          setConfirmModal((prev) => ({ ...prev, isOpen: false }));
          await fetchUsers();
          if (selectedUserDetail && selectedUserDetail.id === userId) {
            setIsDetailModalOpen(false);
          }
        } catch (err: any) {
          toast.error(getErrorMessage(err, 'Failed to delete user account.'));
        } finally {
          setConfirmModal((prev) => ({ ...prev, isLoading: false }));
        }
      }
    });
  };

  return (
    <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8 space-y-8">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-6 border-b border-secondary-200">
        <div>
          <button
            onClick={() => navigate('/dashboard')}
            className="inline-flex items-center text-xs font-semibold text-secondary-500 hover:text-secondary-900 mb-2 transition-colors"
          >
            <ArrowLeft className="h-3.5 w-3.5 mr-1" /> Back to Dashboard
          </button>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-secondary-900 flex items-center gap-3">
            <span className="p-2 bg-primary-100 text-primary-700 rounded-xl">
              <Shield className="h-6 w-6" />
            </span>
            Admin Console & User Control
          </h1>
          <p className="text-secondary-500 text-sm mt-1">
            Manage user authorization roles, security lock states, and account profiles across OdysseyAI.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchUsers}
            disabled={isLoading}
            className="flex items-center gap-2"
          >
            <RotateCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh Data
          </Button>
        </div>
      </div>

      {/* Administrator Profile Card */}
      <div className="bg-gradient-to-r from-purple-900 via-indigo-900 to-purple-950 rounded-3xl p-6 sm:p-7 text-white shadow-lg relative overflow-hidden">
        <div className="absolute -right-8 -top-8 w-64 h-64 bg-white/10 rounded-full blur-3xl pointer-events-none" />
        <div className="flex flex-col sm:flex-row items-center sm:items-start justify-between gap-6 relative z-10">
          <div className="flex flex-col sm:flex-row items-center sm:items-start gap-4">
            <div className="w-16 h-16 rounded-2xl bg-white/20 backdrop-blur-md flex items-center justify-center text-white text-2xl font-extrabold shadow-inner border border-white/30">
              {authUser?.name ? authUser.name.charAt(0).toUpperCase() : 'A'}
            </div>
            <div className="text-center sm:text-left">
              <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2">
                <h2 className="text-xl sm:text-2xl font-bold">{authUser?.name || 'Administrator'}</h2>
                <span className="px-2.5 py-0.5 rounded-full text-xs font-extrabold bg-purple-400/30 text-purple-200 border border-purple-300/30">
                  ROOT ADMIN
                </span>
              </div>
              <p className="text-xs text-purple-200 mt-1 flex items-center justify-center sm:justify-start gap-1.5">
                <Mail className="h-3.5 w-3.5" /> {authUser?.email}
              </p>
              <div className="flex flex-wrap items-center justify-center sm:justify-start gap-3 mt-3 text-xs text-purple-200">
                <span className="flex items-center gap-1">
                  <CheckCircle2 className="h-3.5 w-3.5 text-emerald-400" /> Authorized Admin Session
                </span>
                <span>•</span>
                <span>Role: <strong>{authUser?.role}</strong></span>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2.5">
            <Button
              variant="outline"
              size="sm"
              onClick={() => navigate('/profile')}
              className="bg-white/10 hover:bg-white/20 text-white border-white/20 text-xs flex items-center gap-1.5"
            >
              <UserIcon className="h-3.5 w-3.5" /> Edit Profile & Password
            </Button>
          </div>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        <div className="bg-white p-4 sm:p-5 rounded-2xl border border-secondary-200 shadow-sm">
          <div className="flex items-center justify-between">
            <p className="text-[11px] sm:text-xs font-semibold text-secondary-500 uppercase tracking-wide">Total Users</p>
            <div className="p-1.5 sm:p-2 bg-blue-50 text-blue-600 rounded-lg">
              <Users className="h-4 w-4" />
            </div>
          </div>
          <p className="text-2xl sm:text-3xl font-extrabold text-secondary-900 mt-2">{stats.total}</p>
        </div>

        <div className="bg-white p-4 sm:p-5 rounded-2xl border border-secondary-200 shadow-sm">
          <div className="flex items-center justify-between">
            <p className="text-[11px] sm:text-xs font-semibold text-emerald-600 uppercase tracking-wide">Active Accounts</p>
            <div className="p-1.5 sm:p-2 bg-emerald-50 text-emerald-600 rounded-lg">
              <UserCheck className="h-4 w-4" />
            </div>
          </div>
          <p className="text-2xl sm:text-3xl font-extrabold text-emerald-700 mt-2">{stats.active}</p>
        </div>

        <div className="bg-white p-4 sm:p-5 rounded-2xl border border-secondary-200 shadow-sm">
          <div className="flex items-center justify-between">
            <p className="text-[11px] sm:text-xs font-semibold text-amber-600 uppercase tracking-wide">Locked Accounts</p>
            <div className="p-1.5 sm:p-2 bg-amber-50 text-amber-600 rounded-lg">
              <UserX className="h-4 w-4" />
            </div>
          </div>
          <p className="text-2xl sm:text-3xl font-extrabold text-amber-700 mt-2">{stats.locked}</p>
        </div>

        <div className="bg-white p-4 sm:p-5 rounded-2xl border border-secondary-200 shadow-sm">
          <div className="flex items-center justify-between">
            <p className="text-[11px] sm:text-xs font-semibold text-primary-600 uppercase tracking-wide">Administrators</p>
            <div className="p-1.5 sm:p-2 bg-primary-50 text-primary-600 rounded-lg">
              <ShieldAlert className="h-4 w-4" />
            </div>
          </div>
          <p className="text-2xl sm:text-3xl font-extrabold text-primary-700 mt-2">{stats.admins}</p>
        </div>
      </div>

      {/* Filter & Search Bar */}
      <div className="bg-white p-4 rounded-2xl border border-secondary-200 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="relative w-full sm:w-80">
          <Search className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-secondary-400" />
          <Input
            type="text"
            placeholder="Search by name or email..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9 text-sm"
          />
        </div>

        <div className="flex flex-wrap items-center gap-3 w-full sm:w-auto">
          {/* Role Filter */}
          <div className="flex items-center gap-1.5 text-xs">
            <span className="text-secondary-500 font-medium">Role:</span>
            <select
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value as any)}
              className="bg-secondary-50 border border-secondary-300 text-secondary-800 rounded-lg px-2.5 py-1.5 text-xs focus:ring-primary-500 focus:border-primary-500"
            >
              <option value="ALL">All Roles</option>
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>

          {/* Status Filter */}
          <div className="flex items-center gap-1.5 text-xs">
            <span className="text-secondary-500 font-medium">Status:</span>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="bg-secondary-50 border border-secondary-300 text-secondary-800 rounded-lg px-2.5 py-1.5 text-xs focus:ring-primary-500 focus:border-primary-500"
            >
              <option value="ALL">All Status</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="BLOCKED">BLOCKED</option>
              <option value="SUSPENDED">SUSPENDED</option>
              <option value="PENDING_VERIFICATION">PENDING_VERIFICATION</option>
            </select>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-2xl border border-secondary-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto" style={{ WebkitOverflowScrolling: 'touch' }}>
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-secondary-50/80 border-b border-secondary-200 text-[11px] font-bold text-secondary-500 uppercase tracking-wider whitespace-nowrap">
                <th className="py-3 px-4 sm:px-6">User</th>
                <th className="py-3 px-4 sm:px-6">Email Address</th>
                <th className="py-3 px-4 sm:px-6">Authorization Role</th>
                <th className="py-3 px-4 sm:px-6">Account Status</th>
                <th className="py-3 px-4 sm:px-6 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-secondary-100 text-sm">
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-secondary-500">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <RotateCw className="h-6 w-6 animate-spin text-primary-600" />
                      <span>Loading user accounts...</span>
                    </div>
                  </td>
                </tr>
              ) : filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-secondary-500">
                    <AlertCircle className="h-8 w-8 text-secondary-400 mx-auto mb-2" />
                    <p className="font-semibold text-secondary-700">No matching users found</p>
                    <p className="text-xs text-secondary-400">Try adjusting your search query or filters.</p>
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => {
                  const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'Unnamed User';
                  const isCurrentUser = authUser?.id === user.id;

                  return (
                    <tr key={user.id} className="hover:bg-secondary-50/50 transition-colors">
                      {/* Name & Avatar */}
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-primary-100 text-primary-700 font-bold flex items-center justify-center text-sm shadow-sm border border-primary-200">
                            {user.firstName ? user.firstName.charAt(0).toUpperCase() : (user.email ? user.email.charAt(0).toUpperCase() : 'U')}
                          </div>
                          <div>
                            <div className="font-semibold text-secondary-900 flex items-center gap-2">
                              {fullName}
                              {isCurrentUser && (
                                <span className="text-[10px] px-1.5 py-0.5 bg-secondary-100 text-secondary-600 rounded font-medium">
                                  You
                                </span>
                              )}
                            </div>
                            <span className="text-xs text-secondary-400">ID: #{user.id}</span>
                          </div>
                        </div>
                      </td>

                      {/* Email */}
                      <td className="py-4 px-6 text-secondary-600">
                        <div className="flex items-center gap-1.5 truncate max-w-xs">
                          <Mail className="h-3.5 w-3.5 text-secondary-400 shrink-0" />
                          <span>{user.email}</span>
                        </div>
                      </td>

                      {/* Role */}
                      <td className="py-4 px-6">
                        <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold border ${
                          user.role === 'ADMIN'
                            ? 'bg-purple-50 text-purple-700 border-purple-200'
                            : 'bg-blue-50 text-blue-700 border-blue-200'
                        }`}>
                          {user.role}
                        </span>
                      </td>

                      {/* Status */}
                      <td className="py-4 px-6">
                        <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold border ${
                          user.accountStatus === 'ACTIVE'
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                            : 'bg-amber-50 text-amber-700 border-amber-200'
                        }`}>
                          <span className={`w-1.5 h-1.5 rounded-full mr-1.5 ${user.accountStatus === 'ACTIVE' ? 'bg-emerald-500' : 'bg-amber-500'}`} />
                          {user.accountStatus}
                        </span>
                      </td>

                      {/* Actions */}
                      <td className="py-4 px-6 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          {/* View Details */}
                          <button
                            onClick={() => user.id && handleViewDetail(user.id)}
                            className="p-1.5 text-secondary-500 hover:text-primary-600 rounded-lg hover:bg-secondary-100 transition-colors"
                            title="View Full Profile Details"
                          >
                            <Eye className="h-4 w-4" />
                          </button>

                          {/* Change Role Button */}
                          <button
                            onClick={() => user.id && handleChangeRole(user.id, user.role)}
                            disabled={isCurrentUser}
                            className={`p-1.5 rounded-lg transition-colors ${
                              isCurrentUser
                                ? 'text-secondary-300 cursor-not-allowed'
                                : 'text-secondary-500 hover:text-purple-600 hover:bg-purple-50'
                            }`}
                            title={`Toggle Role to ${user.role === 'ADMIN' ? 'USER' : 'ADMIN'}`}
                          >
                            <Shield className="h-4 w-4" />
                          </button>

                          {/* Lock / Unlock */}
                          {user.accountStatus === 'ACTIVE' ? (
                            <button
                              onClick={() => user.id && handleLockUser(user.id, fullName)}
                              disabled={isCurrentUser}
                              className={`p-1.5 rounded-lg transition-colors ${
                                isCurrentUser
                                  ? 'text-secondary-300 cursor-not-allowed'
                                  : 'text-secondary-500 hover:text-amber-600 hover:bg-amber-50'
                              }`}
                              title="Lock and Suspend User Account"
                            >
                              <Lock className="h-4 w-4" />
                            </button>
                          ) : (
                            <button
                              onClick={() => user.id && handleUnlockUser(user.id, fullName)}
                              className="p-1.5 text-secondary-500 hover:text-emerald-600 rounded-lg hover:bg-emerald-50 transition-colors"
                              title="Unlock User Account"
                            >
                              <Unlock className="h-4 w-4" />
                            </button>
                          )}

                          {/* Delete */}
                          <button
                            onClick={() => user.id && handleDeleteUser(user.id, fullName)}
                            disabled={isCurrentUser}
                            className={`p-1.5 rounded-lg transition-colors ${
                              isCurrentUser
                                ? 'text-secondary-300 cursor-not-allowed'
                                : 'text-secondary-500 hover:text-red-600 hover:bg-red-50'
                            }`}
                            title="Permanently Delete Account"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* User Details Modal */}
      {isDetailModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 sm:p-8 shadow-2xl border border-secondary-200 relative animate-in zoom-in-95">
            <button
              onClick={() => setIsDetailModalOpen(false)}
              className="absolute right-5 top-5 text-secondary-400 hover:text-secondary-700 p-1 rounded-lg hover:bg-secondary-100 transition-colors"
            >
              <X className="h-5 w-5" />
            </button>

            {isDetailLoading ? (
              <div className="py-12 flex flex-col items-center justify-center gap-3">
                <RotateCw className="h-8 w-8 animate-spin text-primary-600" />
                <p className="text-secondary-600 text-sm">Fetching detailed user data...</p>
              </div>
            ) : selectedUserDetail ? (
              <div className="space-y-6">
                <div className="flex items-center gap-4 border-b border-secondary-100 pb-5">
                  <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-primary-600 to-primary-500 text-white font-extrabold flex items-center justify-center text-xl shadow-md">
                    {selectedUserDetail.firstName ? selectedUserDetail.firstName.charAt(0).toUpperCase() : 'U'}
                  </div>
                  <div>
                    <h3 className="text-xl font-bold text-secondary-900">
                      {selectedUserDetail.firstName} {selectedUserDetail.lastName}
                    </h3>
                    <p className="text-xs text-secondary-500">ID: #{selectedUserDetail.id}</p>
                    <div className="flex items-center gap-2 mt-1.5">
                      <span className="px-2 py-0.5 rounded-full text-xs font-bold bg-primary-50 text-primary-700 border border-primary-200">
                        {selectedUserDetail.role}
                      </span>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-bold border ${
                        selectedUserDetail.accountStatus === 'ACTIVE'
                          ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                          : 'bg-amber-50 text-amber-700 border-amber-200'
                      }`}>
                        {selectedUserDetail.accountStatus}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Info Grid */}
                <div className="grid grid-cols-2 gap-4 text-xs">
                  <div className="bg-secondary-50 p-3 rounded-xl">
                    <span className="text-secondary-400 block mb-0.5">Email</span>
                    <span className="font-semibold text-secondary-800 break-all">{selectedUserDetail.email}</span>
                  </div>

                  <div className="bg-secondary-50 p-3 rounded-xl">
                    <span className="text-secondary-400 block mb-0.5">Email Verification</span>
                    <span className="font-semibold text-secondary-800 flex items-center gap-1">
                      {selectedUserDetail.emailVerified ? (
                        <>
                          <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500" /> Verified
                        </>
                      ) : (
                        <>
                          <Clock className="h-3.5 w-3.5 text-amber-500" /> Pending
                        </>
                      )}
                    </span>
                  </div>

                  <div className="bg-secondary-50 p-3 rounded-xl">
                    <span className="text-secondary-400 block mb-0.5">Failed Login Attempts</span>
                    <span className="font-semibold text-secondary-800">{selectedUserDetail.failedLoginAttempts || 0}</span>
                  </div>

                  <div className="bg-secondary-50 p-3 rounded-xl">
                    <span className="text-secondary-400 block mb-0.5">Lock Until</span>
                    <span className="font-semibold text-secondary-800">
                      {selectedUserDetail.accountLockedUntil
                        ? new Date(selectedUserDetail.accountLockedUntil).toLocaleString()
                        : 'Not Locked'}
                    </span>
                  </div>

                  <div className="bg-secondary-50 p-3 rounded-xl">
                    <span className="text-secondary-400 block mb-0.5">Created At</span>
                    <span className="font-semibold text-secondary-800">
                      {selectedUserDetail.createdAt ? new Date(selectedUserDetail.createdAt).toLocaleDateString() : 'N/A'}
                    </span>
                  </div>

                  <div className="bg-secondary-50 p-3 rounded-xl">
                    <span className="text-secondary-400 block mb-0.5">Last Updated</span>
                    <span className="font-semibold text-secondary-800">
                      {selectedUserDetail.updatedAt ? new Date(selectedUserDetail.updatedAt).toLocaleDateString() : 'N/A'}
                    </span>
                  </div>
                </div>

                <div className="flex flex-wrap items-center justify-between gap-3 pt-5 border-t border-secondary-100">
                  <div className="flex flex-wrap items-center gap-2">
                    {/* Toggle Role */}
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => selectedUserDetail.id && handleChangeRole(selectedUserDetail.id, selectedUserDetail.role)}
                      disabled={authUser?.id === selectedUserDetail.id}
                      className="text-xs flex items-center gap-1.5"
                    >
                      <Shield className="h-3.5 w-3.5 text-purple-600" />
                      Make {selectedUserDetail.role === 'ADMIN' ? 'USER' : 'ADMIN'}
                    </Button>

                    {/* Lock / Unlock */}
                    {selectedUserDetail.accountStatus === 'ACTIVE' ? (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => selectedUserDetail.id && handleLockUser(selectedUserDetail.id, `${selectedUserDetail.firstName} ${selectedUserDetail.lastName}`)}
                        disabled={authUser?.id === selectedUserDetail.id}
                        className="text-xs flex items-center gap-1.5 text-amber-600 border-amber-200 hover:bg-amber-50"
                      >
                        <Lock className="h-3.5 w-3.5" />
                        Lock User
                      </Button>
                    ) : (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => selectedUserDetail.id && handleUnlockUser(selectedUserDetail.id, `${selectedUserDetail.firstName} ${selectedUserDetail.lastName}`)}
                        className="text-xs flex items-center gap-1.5 text-emerald-600 border-emerald-200 hover:bg-emerald-50"
                      >
                        <Unlock className="h-3.5 w-3.5" />
                        Unlock User
                      </Button>
                    )}

                    {/* Delete Account */}
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => selectedUserDetail.id && handleDeleteUser(selectedUserDetail.id, `${selectedUserDetail.firstName} ${selectedUserDetail.lastName}`)}
                      disabled={authUser?.id === selectedUserDetail.id}
                      className="text-xs flex items-center gap-1.5 text-red-600 border-red-200 hover:bg-red-50"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                      Purge Account
                    </Button>
                  </div>

                  <Button variant="outline" size="sm" onClick={() => setIsDetailModalOpen(false)}>
                    Close
                  </Button>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      )}

      {/* Confirmation Modal */}
      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title={confirmModal.title}
        message={confirmModal.message}
        confirmText={confirmModal.confirmText}
        variant={confirmModal.variant}
        isLoading={confirmModal.isLoading}
        onConfirm={confirmModal.onConfirm}
        onClose={() => setConfirmModal((prev) => ({ ...prev, isOpen: false }))}
      />
    </div>
  );
};

export default AdminDashboard;
