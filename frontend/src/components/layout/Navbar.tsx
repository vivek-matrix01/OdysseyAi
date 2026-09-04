import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { MapPinned, LogOut, User, Menu, X, Shield } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { getAvatarSrc } from '../../constants/profileAvatars';

const Navbar: React.FC = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const location = useLocation();

  const avatarSrc = getAvatarSrc(
    user?.profilePicture || (user?.id ? localStorage.getItem(`odyssey_user_avatar_${user.id}`) : null)
  );

  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false);
  };

  // Close mobile menu when route changes
  React.useEffect(() => {
    closeMobileMenu();
  }, [location.pathname]);

  return (
    <nav className="bg-white shadow-sm border-b border-secondary-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex">
            <Link to="/" className="flex-shrink-0 flex items-center gap-2">
              <MapPinned className="h-8 w-8 text-primary-600" />
              <span className="font-bold text-xl text-secondary-900 tracking-tight">OdysseyAi</span>
            </Link>
          </div>
          
          {/* Desktop Menu */}
          <div className="hidden md:flex items-center gap-4">
            {isAuthenticated ? (
              <>
                <Link to="/ai-recommendations" className="text-secondary-600 hover:text-primary-600 font-medium text-sm transition-colors">
                  AI Planner
                </Link>
                <Link to="/dashboard" className="text-secondary-600 hover:text-primary-600 font-medium text-sm transition-colors">
                  Dashboard
                </Link>

                {user?.role === 'ADMIN' && (
                  <Link 
                    to="/admin" 
                    className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-purple-50 text-purple-700 hover:bg-purple-100 font-semibold text-xs border border-purple-200 transition-colors"
                  >
                    <Shield className="h-3.5 w-3.5 text-purple-600" />
                    <span>Admin</span>
                  </Link>
                )}
                
                {/* Profile Dropdown */}
                <div className="relative border-l border-secondary-200 pl-4 ml-2">
                  <button 
                    onClick={() => setIsProfileOpen(!isProfileOpen)}
                    className="flex items-center gap-2 text-sm text-secondary-700 hover:text-primary-600 font-medium p-1 rounded-full hover:bg-secondary-50 transition-colors"
                  >
                    <div className="w-8 h-8 rounded-full overflow-hidden bg-primary-100 flex items-center justify-center text-primary-600 font-bold border border-secondary-200 shadow-xs shrink-0">
                      {avatarSrc ? (
                        <img src={avatarSrc} alt={user?.name || "Avatar"} className="w-full h-full object-cover" />
                      ) : (
                        <User className="h-4 w-4" />
                      )}
                    </div>
                    <span className="font-semibold text-xs">{user?.name}</span>
                  </button>

                  {isProfileOpen && (
                    <div className="absolute right-0 mt-2 w-64 bg-white rounded-2xl shadow-xl border border-secondary-200 py-2 z-50 animate-in fade-in slide-in-from-top-2">
                      <div className="px-4 py-3 border-b border-secondary-100 flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full overflow-hidden bg-primary-100 flex items-center justify-center text-primary-600 border border-secondary-200 shadow-xs shrink-0">
                          {avatarSrc ? (
                            <img src={avatarSrc} alt={user?.name || "Avatar"} className="w-full h-full object-cover" />
                          ) : (
                            <User className="h-5 w-5" />
                          )}
                        </div>
                        <div className="overflow-hidden">
                          <p className="text-sm font-bold text-secondary-900 truncate">{user?.name}</p>
                          <p className="text-xs text-secondary-500 truncate">{user?.email || 'User'}</p>
                        </div>
                      </div>
                      {user?.role === 'ADMIN' && (
                        <Link 
                          to="/admin" 
                          onClick={() => setIsProfileOpen(false)}
                          className="flex items-center gap-2 px-4 py-2 text-sm text-purple-700 bg-purple-50/60 hover:bg-purple-100/70 font-semibold transition-colors"
                        >
                          <Shield className="h-4 w-4 text-purple-600" />
                          <span>Admin Console</span>
                        </Link>
                      )}
                      <Link 
                        to="/profile" 
                        onClick={() => setIsProfileOpen(false)}
                        className="flex items-center gap-2 px-4 py-2 text-sm text-secondary-700 hover:bg-secondary-50 transition-colors"
                      >
                        <User className="h-4 w-4 text-primary-500" />
                        <span>Profile & Security</span>
                      </Link>
                      <Link 
                        to="/dashboard" 
                        onClick={() => setIsProfileOpen(false)}
                        className="block px-4 py-2 text-sm text-secondary-700 hover:bg-secondary-50 transition-colors"
                      >
                        My Dashboard
                      </Link>
                      <Link 
                        to="/ai-recommendations" 
                        onClick={() => setIsProfileOpen(false)}
                        className="block px-4 py-2 text-sm text-secondary-700 hover:bg-secondary-50 transition-colors"
                      >
                        AI Travel Planner
                      </Link>
                      <div className="border-t border-secondary-100 mt-1 pt-1">
                        <button
                          onClick={() => { setIsProfileOpen(false); logout(); }}
                          className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 flex items-center gap-2 transition-colors"
                        >
                          <LogOut className="h-4 w-4" /> Log out
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="flex gap-3">
                <Link to="/login" className="text-sm font-medium text-secondary-600 hover:text-secondary-900 px-3 py-2">
                  Log in
                </Link>
                <Link to="/register" className="text-sm font-medium bg-primary-600 text-white px-4 py-2 rounded-md hover:bg-primary-700 transition-colors">
                  Sign up
                </Link>
              </div>
            )}
          </div>

          {/* Mobile menu button */}
          <div className="flex items-center md:hidden">
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="p-2 rounded-md text-secondary-400 hover:text-secondary-500 hover:bg-secondary-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500"
            >
              <span className="sr-only">Open main menu</span>
              {isMobileMenuOpen ? (
                <X className="block h-6 w-6" aria-hidden="true" />
              ) : (
                <Menu className="block h-6 w-6" aria-hidden="true" />
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu Backdrop & Panel */}
      {isMobileMenuOpen && (
        <>
          <div 
            onClick={closeMobileMenu} 
            className="fixed inset-0 bg-black/40 backdrop-blur-xs z-40 md:hidden animate-in fade-in"
            aria-hidden="true"
          />
          <div className="md:hidden bg-white border-b border-secondary-200 shadow-2xl absolute w-full left-0 z-50 animate-in slide-in-from-top-2 duration-200">
            <div className="px-3 pt-2 pb-5 space-y-1.5 sm:px-4">
              {isAuthenticated ? (
                <>
                  <div className="flex items-center px-3 py-3 rounded-2xl bg-secondary-50 border border-secondary-100 mb-3 gap-3">
                    <div className="w-11 h-11 rounded-full overflow-hidden flex-shrink-0 bg-primary-100 flex items-center justify-center text-primary-600 border-2 border-white shadow-xs">
                      {avatarSrc ? (
                        <img src={avatarSrc} alt={user?.name || "Avatar"} className="w-full h-full object-cover" />
                      ) : (
                        <User className="h-5 w-5" />
                      )}
                    </div>
                    <div className="overflow-hidden flex-1">
                      <div className="text-sm font-bold text-secondary-900 truncate">{user?.name}</div>
                      <div className="text-xs text-secondary-500 truncate">{user?.email || 'User'}</div>
                    </div>
                    {user?.role && (
                      <span className="px-2 py-0.5 text-[10px] font-bold bg-primary-100 text-primary-700 rounded-full">
                        {user.role}
                      </span>
                    )}
                  </div>

                  <Link
                    to="/ai-recommendations"
                    onClick={closeMobileMenu}
                    className="flex items-center justify-center gap-2 w-full py-2.5 px-4 rounded-xl text-sm font-bold text-white bg-gradient-to-r from-primary-600 to-primary-500 shadow-md shadow-primary-500/20 mb-2"
                  >
                    <MapPinned className="h-4 w-4" />
                    <span>Plan New Trip with AI</span>
                  </Link>

                  {user?.role === 'ADMIN' && (
                    <Link
                      to="/admin"
                      onClick={closeMobileMenu}
                      className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-bold text-purple-700 bg-purple-50 hover:bg-purple-100 transition-colors min-h-[44px]"
                    >
                      <Shield className="h-4 w-4 text-purple-600" />
                      <span>Admin Console</span>
                    </Link>
                  )}

                  <Link
                    to="/dashboard"
                    onClick={closeMobileMenu}
                    className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-semibold text-secondary-800 hover:bg-secondary-50 transition-colors min-h-[44px]"
                  >
                    <span>My Trips Dashboard</span>
                  </Link>

                  <Link
                    to="/profile"
                    onClick={closeMobileMenu}
                    className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-semibold text-secondary-800 hover:bg-secondary-50 transition-colors min-h-[44px]"
                  >
                    <span>Profile & Security</span>
                  </Link>

                  <button
                    onClick={() => { logout(); closeMobileMenu(); }}
                    className="w-full text-left flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-bold text-red-600 hover:bg-red-50 transition-colors min-h-[44px]"
                  >
                    <LogOut className="h-4 w-4" />
                    <span>Log Out</span>
                  </button>
                </>
              ) : (
                <div className="space-y-2.5 pt-2">
                  <Link
                    to="/ai-recommendations"
                    onClick={closeMobileMenu}
                    className="flex items-center justify-center gap-2 w-full py-3 px-4 rounded-xl text-sm font-bold text-white bg-primary-600 hover:bg-primary-700 shadow-sm min-h-[44px]"
                  >
                    <MapPinned className="h-4 w-4" />
                    <span>Explore AI Planner</span>
                  </Link>
                  <div className="grid grid-cols-2 gap-2 pt-2">
                    <Link
                      to="/login"
                      onClick={closeMobileMenu}
                      className="flex items-center justify-center py-2.5 px-3 border border-secondary-300 rounded-xl shadow-2xs text-sm font-semibold text-secondary-700 bg-white hover:bg-secondary-50 min-h-[44px]"
                    >
                      Log in
                    </Link>
                    <Link
                      to="/register"
                      onClick={closeMobileMenu}
                      className="flex items-center justify-center py-2.5 px-3 rounded-xl shadow-2xs text-sm font-semibold text-white bg-secondary-900 hover:bg-black min-h-[44px]"
                    >
                      Sign up Free
                    </Link>
                  </div>
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </nav>
  );
};

export default Navbar;
