import React, { useState } from 'react';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { AuthPortal } from './components/AuthPortal';
import { UserProfile } from './components/UserProfile';
import { AdminSuite } from './components/AdminSuite';
import { ApiConsole } from './components/ApiConsole';
import { TokenWidget } from './components/TokenWidget';

function MainApp() {
  const [activeTab, setActiveTab] = useState('auth'); // 'auth', 'profile', 'admin', 'api-explorer'

  return (
    <div className="min-h-screen flex flex-col bg-[#090d16] text-slate-100 font-['Plus_Jakarta_Sans',sans-serif]">
      {/* Top Navbar */}
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />

      {/* Main Content Area */}
      <main className="flex-1 pb-24">
        {activeTab === 'auth' && (
          <AuthPortal onLoginSuccess={() => setActiveTab('profile')} />
        )}
        {activeTab === 'profile' && <UserProfile />}
        {activeTab === 'admin' && <AdminSuite />}
        {activeTab === 'api-explorer' && <ApiConsole />}
      </main>

      {/* Floating JWT & API Inspector Widget */}
      <TokenWidget />
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
}
