import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Compass, LogOut, User, Menu, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const Navbar: React.FC = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const location = useLocation();

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
              <Compass className="h-8 w-8 text-primary-600" />
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
                <div className="flex items-center gap-2 border-l border-secondary-200 pl-4 ml-2">
                  <div className="flex items-center gap-2 text-sm text-secondary-700">
                    <User className="h-4 w-4" />
                    <span>{user?.name}</span>
                  </div>
                  <button 
                    onClick={logout}
                    className="p-2 text-secondary-400 hover:text-red-500 rounded-full hover:bg-secondary-50 transition-colors"
                    title="Logout"
                  >
                    <LogOut className="h-5 w-5" />
                  </button>
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

      {/* Mobile Menu Panel */}
      {isMobileMenuOpen && (
        <div className="md:hidden bg-white border-b border-secondary-200 shadow-lg absolute w-full left-0">
          <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3">
            {isAuthenticated ? (
              <>
                <div className="flex items-center px-3 py-3 border-b border-secondary-100 mb-2">
                  <div className="flex-shrink-0 bg-primary-100 p-2 rounded-full text-primary-600">
                    <User className="h-5 w-5" />
                  </div>
                  <div className="ml-3">
                    <div className="text-base font-medium text-secondary-800">{user?.name}</div>
                    <div className="text-sm font-medium text-secondary-500">{user?.email || 'User'}</div>
                  </div>
                </div>
                <Link
                  to="/dashboard"
                  className="block px-3 py-2 rounded-md text-base font-medium text-secondary-900 hover:bg-secondary-50"
                >
                  Dashboard
                </Link>
                <Link
                  to="/ai-recommendations"
                  className="block px-3 py-2 rounded-md text-base font-medium text-primary-600 hover:bg-primary-50"
                >
                  AI Planner
                </Link>
                <button
                  onClick={() => { logout(); closeMobileMenu(); }}
                  className="w-full text-left block px-3 py-2 rounded-md text-base font-medium text-red-600 hover:bg-red-50"
                >
                  Log out
                </button>
              </>
            ) : (
              <div className="space-y-2 p-3">
                <Link
                  to="/login"
                  className="w-full flex justify-center py-2 px-4 border border-secondary-300 rounded-md shadow-sm text-base font-medium text-secondary-700 bg-white hover:bg-secondary-50"
                >
                  Log in
                </Link>
                <Link
                  to="/register"
                  className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-base font-medium text-white bg-primary-600 hover:bg-primary-700"
                >
                  Sign up
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
