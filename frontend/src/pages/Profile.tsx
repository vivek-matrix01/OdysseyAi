import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { 
  User as UserIcon, 
  Mail, 
  ShieldCheck, 
  Lock, 
  KeyRound, 
  Loader2, 
  CheckCircle2, 
  ArrowLeft,
  Save,
  RotateCw,
  Camera,
  Sparkles,
  Check
} from 'lucide-react';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import BorderGlow from '../components/ui/BorderGlow';
import { useAuth } from '../context/AuthContext';
import { UserControllerService } from '../api/generated';
import type { User as ApiUser } from '../api/generated';
import { PROFILE_AVATARS, getAvatarSrc } from '../constants/profileAvatars';

const Profile: React.FC = () => {
  const navigate = useNavigate();
  const { user: authUser, refreshUser } = useAuth();

  // Profile data state
  const [profile, setProfile] = useState<ApiUser | null>(null);
  const [isFetching, setIsFetching] = useState<boolean>(true);

  // Avatar state
  const [selectedAvatar, setSelectedAvatar] = useState<string>('');
  const [isUpdatingAvatar, setIsUpdatingAvatar] = useState<boolean>(false);

  // Profile update state
  const [firstName, setFirstName] = useState<string>('');
  const [lastName, setLastName] = useState<string>('');
  const [isUpdatingProfile, setIsUpdatingProfile] = useState<boolean>(false);

  // Password update state
  const [currentPassword, setCurrentPassword] = useState<string>('');
  const [newPassword, setNewPassword] = useState<string>('');
  const [confirmPassword, setConfirmPassword] = useState<string>('');
  const [isChangingPassword, setIsChangingPassword] = useState<boolean>(false);

  const getErrorMessage = (err: any, fallback: string) => {
    return err?.body?.message || err?.response?.data?.message || err?.message || fallback;
  };

  // Load profile from GET /api/users/me
  const fetchProfile = async () => {
    setIsFetching(true);
    try {
      const response = await UserControllerService.getCurrentProfile();
      if (response.data) {
        setProfile(response.data);
        setFirstName(response.data.firstName || '');
        setLastName(response.data.lastName || '');
        const savedAvatar = (response.data as any).profilePicture || (response.data.id ? localStorage.getItem(`odyssey_user_avatar_${response.data.id}`) : '') || '';
        setSelectedAvatar(savedAvatar);
      }
    } catch (err: any) {
      toast.error(getErrorMessage(err, 'Failed to load profile details.'));
    } finally {
      setIsFetching(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  // Handle Avatar Selection & Instant Save
  const handleApplyAvatar = async (avatarId: string) => {
    setSelectedAvatar(avatarId);
    setIsUpdatingAvatar(true);
    try {
      if (profile?.id) {
        localStorage.setItem(`odyssey_user_avatar_${profile.id}`, avatarId);
      }
      await UserControllerService.updateProfile({
        firstName: firstName.trim() || profile?.firstName || '',
        lastName: lastName.trim() || profile?.lastName || '',
        profilePicture: avatarId,
      });
      toast.success(avatarId ? 'Profile picture updated!' : 'Profile picture reset to initials.');
      await fetchProfile();
      await refreshUser();
    } catch (err: any) {
      toast.error(getErrorMessage(err, 'Failed to update profile picture.'));
    } finally {
      setIsUpdatingAvatar(false);
    }
  };

  // Handle Profile Update (PUT /api/users/me)
  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!firstName.trim() || !lastName.trim()) {
      toast.error('First name and last name are required.');
      return;
    }

    setIsUpdatingProfile(true);
    try {
      await UserControllerService.updateProfile({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        profilePicture: selectedAvatar,
      });

      if (profile?.id && selectedAvatar) {
        localStorage.setItem(`odyssey_user_avatar_${profile.id}`, selectedAvatar);
      }

      toast.success('Profile updated successfully!');
      await fetchProfile();
      await refreshUser();
    } catch (err: any) {
      toast.error(getErrorMessage(err, 'Failed to update profile.'));
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  // Handle Password Change (PUT /api/users/me/change-password)
  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!currentPassword) {
      toast.error('Please enter your current password.');
      return;
    }

    if (!newPassword || newPassword.length < 6) {
      toast.error('New password must be at least 6 characters long.');
      return;
    }

    if (newPassword !== confirmPassword) {
      toast.error('New password and confirm password do not match.');
      return;
    }

    setIsChangingPassword(true);
    try {
      await UserControllerService.changePassword({
        currentPassword,
        newPassword
      });

      toast.success('Password changed successfully!');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err: any) {
      toast.error(getErrorMessage(err, 'Failed to change password. Please check your current password.'));
    } finally {
      setIsChangingPassword(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8 space-y-8">
      {/* Top Navigation */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate('/dashboard')}
          className="inline-flex items-center text-sm font-medium text-secondary-600 hover:text-secondary-900 transition-colors"
        >
          <ArrowLeft className="h-4 w-4 mr-1.5" /> Back to Dashboard
        </button>

        <Button
          variant="outline"
          size="sm"
          onClick={fetchProfile}
          disabled={isFetching}
          className="flex items-center gap-1.5"
        >
          <RotateCw className={`h-3.5 w-3.5 ${isFetching ? 'animate-spin' : ''}`} />
          Refresh Profile
        </Button>
      </div>

      {/* Admin Quick Action Banner if user is ADMIN */}
      {profile?.role === 'ADMIN' && (
        <div className="bg-gradient-to-r from-purple-50 via-indigo-50 to-purple-50 border border-purple-200 rounded-2xl p-5 flex flex-col sm:flex-row items-center justify-between gap-4 shadow-sm">
          <div className="flex items-center gap-3.5">
            <div className="p-3 bg-purple-100 text-purple-700 rounded-2xl shrink-0">
              <ShieldCheck className="h-6 w-6" />
            </div>
            <div>
              <h2 className="text-base font-bold text-purple-950 flex items-center gap-2">
                Administrator Privileges Active
                <span className="text-[11px] font-extrabold px-2 py-0.5 rounded-full bg-purple-200 text-purple-800">
                  SYSTEM ADMIN
                </span>
              </h2>
              <p className="text-xs text-purple-800/80 mt-0.5">
                You have full access to manage user roles, lock/unlock accounts, and purge accounts in the Admin Console.
              </p>
            </div>
          </div>
          <Button
            onClick={() => navigate('/admin')}
            className="shrink-0 bg-purple-700 hover:bg-purple-800 text-white flex items-center gap-2 text-xs font-semibold px-4 py-2.5 shadow-sm"
          >
            <ShieldCheck className="h-4 w-4" /> Open Admin Console
          </Button>
        </div>
      )}

      {/* Profile Header Card */}
      <div className="bg-gradient-to-r from-primary-600 to-primary-700 rounded-3xl p-6 sm:p-8 text-white shadow-md relative overflow-hidden">
        <div className="absolute right-0 top-0 translate-x-8 -translate-y-8 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none" />
        
        <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6 relative z-10">
          <div className="relative group">
            <div className="w-24 h-24 rounded-3xl bg-white/20 backdrop-blur-md flex items-center justify-center text-white text-3xl font-extrabold shadow-lg border-2 border-white/40 overflow-hidden">
              {getAvatarSrc(selectedAvatar) ? (
                <img 
                  src={getAvatarSrc(selectedAvatar)!} 
                  alt="Profile Avatar" 
                  className="w-full h-full object-cover" 
                />
              ) : (
                profile?.firstName ? profile.firstName.charAt(0).toUpperCase() : (authUser?.email?.charAt(0).toUpperCase() || 'U')
              )}
            </div>
            <div className="absolute -bottom-1.5 -right-1.5 bg-white text-primary-600 p-1.5 rounded-xl shadow-md border border-primary-100">
              <Camera className="h-4 w-4" />
            </div>
          </div>

          <div className="text-center sm:text-left flex-1">
            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2.5 mb-1">
              <h1 className="text-2xl sm:text-3xl font-bold">
                {profile ? `${profile.firstName || ''} ${profile.lastName || ''}`.trim() || 'User Profile' : 'Loading...'}
              </h1>
              {profile?.role && (
                <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-white/20 text-white backdrop-blur-sm border border-white/20">
                  {profile.role}
                </span>
              )}
            </div>

            <p className="text-primary-100 flex items-center justify-center sm:justify-start gap-2 text-sm mt-1">
              <Mail className="h-4 w-4" /> {profile?.email || authUser?.email}
            </p>

            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-4 mt-4 pt-4 border-t border-white/15 text-xs text-primary-100">
              <span className="flex items-center gap-1">
                <ShieldCheck className="h-4 w-4 text-primary-200" />
                Status: <strong className="text-white">{profile?.accountStatus || 'ACTIVE'}</strong>
              </span>
              <span className="flex items-center gap-1">
                <CheckCircle2 className="h-4 w-4 text-emerald-300" />
                {profile?.emailVerified ? 'Email Verified' : 'Account Active'}
              </span>
              {selectedAvatar && (
                <span className="flex items-center gap-1 bg-white/20 px-2.5 py-0.5 rounded-full text-[11px] font-semibold border border-white/20">
                  <Sparkles className="h-3 w-3 text-amber-300" />
                  Custom Avatar Active
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Choose Profile Avatar Section */}
      <div className="bg-white rounded-3xl border border-secondary-200 shadow-sm p-4 sm:p-7 space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-4 border-b border-secondary-100">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-primary-50 text-primary-600 rounded-xl">
              <Sparkles className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base sm:text-lg font-bold text-secondary-900">Choose Profile Picture</h2>
              <p className="text-xs text-secondary-500">
                Select one of the 5 travel personas from your profile collection
              </p>
            </div>
          </div>

          {selectedAvatar && (
            <button
              type="button"
              onClick={() => handleApplyAvatar('')}
              className="text-xs font-semibold text-secondary-500 hover:text-red-600 transition-colors self-start sm:self-auto"
            >
              Reset to Default Initials
            </button>
          )}
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3 sm:gap-4">
          {PROFILE_AVATARS.map((avatar) => {
            const isSelected = selectedAvatar === avatar.id || selectedAvatar === avatar.filename;
            return (
              <BorderGlow
                key={avatar.id}
                edgeSensitivity={30}
                glowColor="40 80 80"
                backgroundColor="#ffffff"
                borderRadius={20}
                glowRadius={30}
                glowIntensity={1}
                coneSpread={25}
                colors={['#c084fc', '#f472b6', '#38bdf8']}
                className={`cursor-pointer transition-all ${
                  isSelected ? 'ring-4 ring-primary-500 shadow-md scale-[1.02]' : 'hover:shadow-md'
                }`}
              >
                <div
                  onClick={() => handleApplyAvatar(avatar.id)}
                  className="p-3.5 flex flex-col items-center text-center h-full justify-between"
                >
                  <div className="relative w-20 h-20 sm:w-24 sm:h-24 rounded-2xl overflow-hidden mb-3 shadow-inner bg-secondary-100">
                    <img
                      src={avatar.src}
                      alt={avatar.name}
                      className="w-full h-full object-cover transition-transform duration-500 hover:scale-110"
                    />
                    {isSelected && (
                      <div className="absolute top-1.5 right-1.5 bg-primary-600 text-white p-1 rounded-full shadow-xs">
                        <Check className="h-3.5 w-3.5" />
                      </div>
                    )}
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-secondary-900 leading-snug">{avatar.name}</h4>
                    <p className="text-[10px] text-secondary-500 mt-0.5">{avatar.subtitle}</p>
                  </div>
                  <button
                    type="button"
                    disabled={isUpdatingAvatar}
                    onClick={(e) => {
                      e.stopPropagation();
                      handleApplyAvatar(avatar.id);
                    }}
                    className={`mt-3 w-full text-[11px] font-bold py-1.5 px-2 rounded-xl transition-all ${
                      isSelected
                        ? 'bg-primary-600 text-white shadow-xs'
                        : 'bg-secondary-100 text-secondary-700 hover:bg-primary-50 hover:text-primary-700'
                    }`}
                  >
                    {isSelected ? 'Active Avatar' : 'Select'}
                  </button>
                </div>
              </BorderGlow>
            );
          })}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 sm:gap-8">
        {/* Section 1: Update Profile Details */}
        <div className="bg-white rounded-2xl border border-secondary-200 shadow-sm p-5 sm:p-7 space-y-5">
          <div className="flex items-center gap-3 pb-4 border-b border-secondary-100">
            <div className="p-2.5 bg-primary-50 text-primary-600 rounded-xl">
              <UserIcon className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-secondary-900">Personal Information</h2>
              <p className="text-xs text-secondary-500">Update your name and profile identity</p>
            </div>
          </div>

          <form onSubmit={handleUpdateProfile} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-secondary-700 mb-1">
                Email Address
              </label>
              <Input
                type="email"
                disabled
                value={profile?.email || authUser?.email || ''}
                className="bg-secondary-50 text-secondary-500 cursor-not-allowed"
              />
              <p className="text-[11px] text-secondary-400 mt-1">Email address cannot be changed directly.</p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-secondary-700 mb-1">
                  First Name
                </label>
                <Input
                  type="text"
                  required
                  placeholder="e.g. Vivek"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-secondary-700 mb-1">
                  Last Name
                </label>
                <Input
                  type="text"
                  required
                  placeholder="e.g. Sharma"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                />
              </div>
            </div>

            <div className="pt-3">
              <Button
                type="submit"
                disabled={isUpdatingProfile}
                fullWidth
                className="flex items-center justify-center gap-2"
              >
                {isUpdatingProfile ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" /> Saving Changes...
                  </>
                ) : (
                  <>
                    <Save className="h-4 w-4" /> Save Profile Details
                  </>
                )}
              </Button>
            </div>
          </form>
        </div>

        {/* Section 2: Update Password */}
        <div className="bg-white rounded-2xl border border-secondary-200 shadow-sm p-5 sm:p-7 space-y-5">
          <div className="flex items-center gap-3 pb-4 border-b border-secondary-100">
            <div className="p-2.5 bg-amber-50 text-amber-600 rounded-xl">
              <Lock className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-secondary-900">Security & Password</h2>
              <p className="text-xs text-secondary-500">Update your account login password</p>
            </div>
          </div>

          <form onSubmit={handleChangePassword} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-secondary-700 mb-1 flex items-center justify-between">
                <span>Current Password</span>
              </label>
              <Input
                type="password"
                required
                placeholder="Enter current password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-secondary-700 mb-1 flex items-center justify-between">
                <span>New Password</span>
                <span className="text-[11px] text-secondary-400">Min. 6 characters</span>
              </label>
              <Input
                type="password"
                required
                placeholder="Enter new password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-secondary-700 mb-1">
                Confirm New Password
              </label>
              <Input
                type="password"
                required
                placeholder="Re-type new password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
            </div>

            <div className="pt-3">
              <Button
                type="submit"
                variant="primary"
                disabled={isChangingPassword}
                fullWidth
                className="flex items-center justify-center gap-2 bg-amber-600 hover:bg-amber-700 text-white"
              >
                {isChangingPassword ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" /> Updating Password...
                  </>
                ) : (
                  <>
                    <KeyRound className="h-4 w-4" /> Change Password
                  </>
                )}
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Profile;