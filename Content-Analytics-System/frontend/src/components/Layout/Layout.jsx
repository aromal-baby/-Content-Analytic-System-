import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
    LayoutDashboard,
    Users,
    Home,
    PlusCircle,
    LogOut,
    Search,
    Sun,
    Moon
} from 'lucide-react';
import AddPlatformModal from '../Platforms/AddPlatformModal';
import { useAuth } from "../../context/AuthContext";
import { useTheme } from "../../context/ThemeContext"; // Import useTheme

const Sidebar = () => {
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const location = useLocation();
    const { user } = useAuth();
    const { darkMode } = useTheme(); // Get darkMode state

    const directLogout = () => {
        localStorage.removeItem('token');
        window.location.href = '/login';
    };

    const isActive = (path) => {
        return location.pathname === path;
    };

    // Use CSS variables for theme-aware styling
    return (
        <div className="fixed left-0 top-0 h-full w-64 border-r flex flex-col"
             style={{
                 backgroundColor: 'var(--sidebar-bg)',
                 borderColor: 'var(--border-color)',
                 color: 'var(--text-secondary)'
             }}>
            {/* Profile Section */}
            <div className="flex flex-col items-center py-8 border-b"
                 style={{ borderColor: 'var(--border-color)' }}>
                <div className="w-20 h-20 rounded-full bg-gradient-to-r from-blue-500 to-teal-400 flex items-center justify-center mb-4 overflow-hidden">
                    <img
                        src={`https://ui-avatars.com/api/?name=${user?.name || 'User'}&background=0D8ABC&color=fff`}
                        alt="Profile"
                        className="w-full h-full object-cover"
                    />
                </div>
                <h2 className="text-xl font-semibold" style={{ color: 'var(--text-primary)' }}>{user?.name || 'User'}</h2>
                <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>Content Manager</p>
            </div>

            {/* Navigation */}
            <div className="flex-grow py-6 px-4">
                <h3 className="text-xs uppercase font-semibold mb-4 px-2" style={{ color: 'var(--text-secondary)' }}>Main</h3>
                <nav>
                    <ul className="space-y-1">
                        <li>
                            <Link
                                to="/dashboard"
                                className={`flex items-center px-4 py-3 rounded-lg transition-colors ${
                                    isActive('/dashboard')
                                        ? 'bg-gradient-to-r from-blue-600/40 to-teal-600/40 text-white'
                                        : 'hover:bg-opacity-20 hover:bg-gray-500'
                                }`}
                                style={{ color: isActive('/dashboard') ? '#fff' : 'var(--text-primary)' }}
                            >
                                <LayoutDashboard size={18} className="mr-3" />
                                Dashboard
                            </Link>
                        </li>
                        <li>
                            <Link
                                to="/"
                                className={`flex items-center px-4 py-3 rounded-lg transition-colors ${
                                    isActive('/')
                                        ? 'bg-gradient-to-r from-blue-600/40 to-teal-600/40 text-white'
                                        : 'hover:bg-opacity-20 hover:bg-gray-500'
                                }`}
                                style={{ color: isActive('/') ? '#fff' : 'var(--text-primary)' }}
                            >
                                <Home size={18} className="mr-3" />
                                Home
                            </Link>
                        </li>
                        <li>
                            <Link
                                to="/platforms"
                                className={`flex items-center px-4 py-3 rounded-lg transition-colors ${
                                    isActive('/platforms')
                                        ? 'bg-gradient-to-r from-blue-600/40 to-teal-600/40 text-white'
                                        : 'hover:bg-opacity-20 hover:bg-gray-500'
                                }`}
                                style={{ color: isActive('/platforms') ? '#fff' : 'var(--text-primary)' }}
                            >
                                <Users size={18} className="mr-3" />
                                Platforms
                            </Link>
                        </li>
                    </ul>
                </nav>
            </div>

            {/* Logout Button */}
            <div className="p-4 border-t" style={{ borderColor: 'var(--border-color)' }}>
                <button
                    type="button"
                    onClick={directLogout}
                    className="w-full flex items-center px-4 py-3 rounded-lg hover:bg-red-500/20 hover:text-red-400 transition-colors"
                    style={{ color: 'var(--text-secondary)' }}
                >
                    <LogOut size={18} className="mr-3" />
                    Logout
                </button>
            </div>

            {/* Add Platform Button */}
            <div
                onClick={() => setIsAddModalOpen(true)}
                className="fixed bottom-6 right-6 bg-gradient-to-r from-blue-500 to-teal-400 rounded-full p-3 shadow-lg cursor-pointer transition-all hover:scale-105"
            >
                <PlusCircle size={24} />
            </div>

            {/* Add Platform Modal */}
            <AddPlatformModal
                isOpen={isAddModalOpen}
                onClose={() => setIsAddModalOpen(false)}
            />
        </div>
    );
};

const Layout = ({ children }) => {
    const { darkMode, toggleTheme } = useTheme(); // Get theme state and toggle function

    return (
        <div style={{ backgroundColor: 'var(--bg-primary)', minHeight: '100vh' }} className="flex">
            <Sidebar />
            <div className="ml-64 flex-grow">
                {/* Header */}
                <header className="border-b py-3 px-6 flex justify-between items-center"
                        style={{
                            backgroundColor: 'var(--header-bg)',
                            borderColor: 'var(--border-color)',
                            color: 'var(--text-primary)'
                        }}>
                    <div className="flex items-center">
                        <h1 className="text-3xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-500">CAS.</h1>
                        <p className="text-sm ml-2" style={{ color: 'var(--text-secondary)' }}>Welcome to your analytics hub</p>
                    </div>
                    <div className="flex items-center space-x-4">
                        {/* Theme Toggle Button */}
                        <div
                            className={`theme-toggle ${darkMode ? 'dark' : 'light'}`}
                            onClick={toggleTheme}
                        >
                            <div className="toggle-thumb">
                                {darkMode ? (
                                    <Moon size={14} className={`toggle-icon ${darkMode ? 'visible' : ''}`} />
                                ) : (
                                    <Sun size={14} className={`toggle-icon ${!darkMode ? 'visible' : ''}`} />
                                )}
                            </div>
                        </div>

                        <div className="relative">
                            <input
                                type="text"
                                placeholder="Search..."
                                className="border rounded-md py-2 pl-10 pr-4 text-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
                                style={{
                                    backgroundColor: darkMode ? '#2d3748' : '#f3f4f6',
                                    borderColor: 'var(--border-color)',
                                    color: 'var(--text-primary)'
                                }}
                            />
                            <Search size={16} className="absolute left-3 top-2.5" style={{ color: 'var(--text-secondary)' }} />
                        </div>
                        <button className="bg-gradient-to-r from-blue-600 to-teal-500 text-white px-4 py-2 rounded-md text-sm font-medium flex items-center">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                            </svg>
                            DOWNLOAD REPORTS
                        </button>
                    </div>
                </header>
                <main className="p-6">
                    {children}
                </main>
            </div>
        </div>
    );
};

export default Layout;