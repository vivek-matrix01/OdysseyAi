import React from 'react';
import { Outlet, useLocation, Link } from 'react-router-dom';
import { MapPinned, Sparkles } from 'lucide-react';
import Navbar from './Navbar';

const Layout: React.FC = () => {
  const location = useLocation();
  const isLandingPage = location.pathname === '/';

  return (
    <div className="min-h-screen flex flex-col bg-secondary-50">
      <Navbar />
      <main className={`flex-grow w-full ${isLandingPage ? '' : 'max-w-7xl mx-auto px-3.5 sm:px-6 lg:px-8 py-5 sm:py-8'}`}>
        <Outlet />
      </main>
      
      {/* Rich Footer */}
      <footer className="bg-secondary-950 text-secondary-300 border-t border-secondary-800 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-12">
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-8 mb-10">
            {/* Column 1: Brand */}
            <div className="space-y-3">
              <Link to="/" className="flex items-center gap-2 text-white">
                <MapPinned className="h-7 w-7 text-primary-400" />
                <span className="font-bold text-xl tracking-tight">OdysseyAi</span>
              </Link>
              <p className="text-xs text-secondary-400 leading-relaxed">
                Next-generation autonomous travel planning powered by real-time intelligence, Google Places, and SerpApi verified stays.
              </p>
            </div>

            {/* Column 2: Navigation */}
            <div>
              <h4 className="text-xs font-bold text-white uppercase tracking-wider mb-3">Product & Tools</h4>
              <ul className="space-y-2 text-xs">
                <li><Link to="/ai-recommendations" className="hover:text-white transition-colors">AI Trip Planner</Link></li>
                <li><Link to="/dashboard" className="hover:text-white transition-colors">My Dashboard</Link></li>
                <li><Link to="/profile" className="hover:text-white transition-colors">Profile & Security</Link></li>
              </ul>
            </div>

            {/* Column 3: Capabilities */}
            <div>
              <h4 className="text-xs font-bold text-white uppercase tracking-wider mb-3">Key Features</h4>
              <ul className="space-y-2 text-xs text-secondary-400">
                <li>Live SerpApi Hotel Search</li>
                <li>Real Road Fuel Calculations</li>
                <li>Integrated Weather Forecasts</li>
                <li>3-Tier Team Permissions</li>
              </ul>
            </div>

            {/* Column 4: Quick Action */}
            <div>
              <h4 className="text-xs font-bold text-white uppercase tracking-wider mb-3">Get Started</h4>
              <p className="text-xs text-secondary-400 mb-3">
                Experience seamless, automated travel planning today.
              </p>
              <Link 
                to="/register" 
                className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg bg-primary-600 hover:bg-primary-500 text-white text-xs font-semibold shadow-xs transition-colors"
              >
                <Sparkles className="h-3.5 w-3.5" /> Sign up Free
              </Link>
            </div>
          </div>

          <div className="pt-8 border-t border-secondary-800/80 text-center sm:flex sm:justify-between sm:text-left text-xs text-secondary-500">
            <div>
              &copy; {new Date().getFullYear()} OdysseyAi Travel Planner. All rights reserved.
            </div>
            <div className="mt-2 sm:mt-0 flex items-center justify-center gap-4">
              <span>Made for travelers worldwide</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Layout;
