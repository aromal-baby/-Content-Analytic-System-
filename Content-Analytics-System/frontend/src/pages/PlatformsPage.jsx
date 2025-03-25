import React, { useState, useEffect } from 'react';
import { PlusCircle, ExternalLink, BarChart2, RefreshCw } from 'lucide-react';
import platformService from '../services/PlatformService';
import metricsService from '../services/MetricsService';
import PlatformMetricsModal from '../components/Platforms/PlatformMetricsModal';
import AddPlatformModal from '../components/Platforms/AddPlatformModal';
import { useTheme } from '../context/ThemeContext';

const PlatformsPage = () => {
    const [platforms, setPlatforms] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedPlatform, setSelectedPlatform] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [refreshing, setRefreshing] = useState({});
    const { darkMode } = useTheme();

    // Platform icons mapping
    const platformIcons = {
        YouTube: "text-red-500",
        Instagram: "text-pink-500",
        TikTok: "text-blue-400",
        Twitter: "text-blue-500",
        Facebook: "text-blue-600",
        LinkedIn: "text-blue-700",
        default: "text-gray-500"
    };

    useEffect(() => {
        fetchPlatforms();
    }, []);

    const fetchPlatforms = async () => {
        try {
            setLoading(true);
            const data = await platformService.getUserPlatforms();
            setPlatforms(data);
        } catch (error) {
            console.error("Failed to fetch platforms:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleViewMetrics = (platform) => {
        setSelectedPlatform(platform);
        setIsModalOpen(true);
    };

    const handleRefreshMetrics = async (platformId) => {
        try {
            setRefreshing(prev => ({ ...prev, [platformId]: true }));
            await metricsService.refreshPlatformMetrics(platformId);
            // Optionally update the platform data after refresh
            fetchPlatforms();
        } catch (error) {
            console.error(`Failed to refresh metrics for platform ${platformId}:`, error);
        } finally {
            setRefreshing(prev => ({ ...prev, [platformId]: false }));
        }
    };

    const getPlatformIcon = (platformName) => {
        switch(platformName.toLowerCase()) {
            case 'youtube':
                return (
                    <svg className="h-10 w-10 text-red-500" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
                    </svg>
                );
            case 'instagram':
                return (
                    <svg className="h-10 w-10 text-pink-500" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z"/>
                    </svg>
                );
            case 'tiktok':
                return (
                    <svg className="h-10 w-10 text-black" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M19.59 6.69a4.83 4.83 0 0 1-3.77-4.25V2h-3.45v13.67a2.89 2.89 0 0 1-5.2 1.74 2.89 2.89 0 0 1 2.31-4.64 2.93 2.93 0 0 1 .88.13V9.4a6.84 6.84 0 0 0-1-.05A6.33 6.33 0 0 0 5 20.1a6.34 6.34 0 0 0 10.86-4.43v-7a8.16 8.16 0 0 0 4.77 1.52v-3.4a4.85 4.85 0 0 1-1-.1z"/>
                    </svg>
                );
            default:
                return (
                    <svg className="h-10 w-10 text-gray-400" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/>
                        <path d="M0 0h24v24H0z" fill="none"/>
                    </svg>
                );
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-96">
                <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-blue-500"></div>
            </div>
        );
    }

    return (
        <div>
            <div className="flex justify-between items-center mb-8">
                <h1 className="text-3xl font-bold" style={{ color: 'var(--text-primary)' }}>Connected Platforms</h1>
                <button
                    onClick={() => setIsAddModalOpen(true)}
                    className="bg-gradient-to-r from-blue-600 to-teal-500 text-white px-4 py-2 rounded-md text-sm font-medium flex items-center"
                >
                    <PlusCircle size={16} className="mr-2" />
                    Add Platform
                </button>
            </div>

            {platforms.length === 0 ? (
                <div
                    className="border rounded-lg p-8 text-center"
                    style={{
                        backgroundColor: 'var(--card-bg)',
                        borderColor: 'var(--border-color)',
                        color: 'var(--text-secondary)'
                    }}
                >
                    <h2 className="text-xl font-semibold mb-2" style={{ color: 'var(--text-primary)' }}>No Platforms Connected</h2>
                    <p className="mb-4">Connect your social media platforms to start tracking analytics</p>
                    <button
                        onClick={() => setIsAddModalOpen(true)}
                        className="bg-gradient-to-r from-blue-600 to-teal-500 text-white px-4 py-2 rounded-md text-sm font-medium inline-flex items-center"
                    >
                        <PlusCircle size={16} className="mr-2" />
                        Add Your First Platform
                    </button>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {platforms.map((platform) => (
                        <div
                            key={platform.id}
                            className="border rounded-lg overflow-hidden shadow-sm"
                            style={{
                                backgroundColor: 'var(--card-bg)',
                                borderColor: 'var(--border-color)'
                            }}
                        >
                            <div className="p-6">
                                <div className="flex items-center mb-4">
                                    {getPlatformIcon(platform.platformName)}
                                    <div className="ml-4">
                                        <h3 className="text-lg font-semibold" style={{ color: 'var(--text-primary)' }}>{platform.platformName}</h3>
                                        <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>
                                            {platform.platformUsername || 'Connected Account'}
                                        </p>
                                    </div>
                                </div>

                                <div className="flex items-center text-sm mt-2 mb-6">
                                    <span className="flex items-center" style={{ color: 'var(--text-secondary)' }}>
                                        <svg className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                        </svg>
                                        Connected {new Date(platform.createdAt).toLocaleDateString()}
                                    </span>
                                    <span className="ml-4 flex items-center text-green-500">
                                        <svg className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                                        </svg>
                                        Active
                                    </span>
                                </div>

                                <div className="grid grid-cols-2 gap-4 mb-4">
                                    <button
                                        onClick={() => handleViewMetrics(platform)}
                                        className="flex items-center justify-center py-2 px-3 rounded-md text-sm font-medium"
                                        style={{
                                            color: 'var(--text-primary)',
                                            backgroundColor: darkMode ? 'rgba(59, 130, 246, 0.2)' : 'rgba(59, 130, 246, 0.1)'
                                        }}
                                    >
                                        <BarChart2 size={16} className="mr-2 text-blue-500" />
                                        View Metrics
                                    </button>
                                    <button
                                        onClick={() => handleRefreshMetrics(platform.id)}
                                        className="flex items-center justify-center py-2 px-3 rounded-md text-sm font-medium"
                                        disabled={refreshing[platform.id]}
                                        style={{
                                            color: 'var(--text-primary)',
                                            backgroundColor: darkMode ? 'rgba(16, 185, 129, 0.2)' : 'rgba(16, 185, 129, 0.1)'
                                        }}
                                    >
                                        <RefreshCw
                                            size={16}
                                            className={`mr-2 text-teal-500 ${refreshing[platform.id] ? 'animate-spin' : ''}`}
                                        />
                                        {refreshing[platform.id] ? 'Refreshing...' : 'Refresh'}
                                    </button>
                                </div>

                                <a
                                    href="#"
                                    className="flex items-center justify-center mt-2 text-sm font-medium"
                                    style={{ color: 'var(--text-secondary)' }}
                                >
                                    <ExternalLink size={14} className="mr-1.5" />
                                    View Profile
                                </a>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Platform Metrics Modal */}
            <PlatformMetricsModal
                platform={selectedPlatform}
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
            />

            {/* Add Platform Modal */}
            <AddPlatformModal
                isOpen={isAddModalOpen}
                onClose={() => setIsAddModalOpen(false)}
            />
        </div>
    );
};

export default PlatformsPage;