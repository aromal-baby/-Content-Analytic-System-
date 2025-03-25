import React, { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, BarChart, Bar } from 'recharts';
import metricsService from '../../services/MetricsService';
import { X } from 'lucide-react';

const PlatformMetricsModal = ({ platform, isOpen, onClose }) => {
    const [loading, setLoading] = useState(true);
    const [metrics, setMetrics] = useState(null);
    const [timeSeriesData, setTimeSeriesData] = useState([]);
    const [error, setError] = useState(null);
    const [contentList, setContentList] = useState([]);

    const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042'];

    useEffect(() => {
        if (isOpen && platform) {
            fetchPlatformMetrics();
            fetchPlatformContent();
        }
    }, [isOpen, platform]);

    const fetchPlatformMetrics = async () => {
        try {
            setLoading(true);
            setError(null);

            // Fetch platform-specific metrics
            const response = await metricsService.getPlatformSpecificMetrics(platform.id);
            console.log("Platform metrics:", response);

            setMetrics(response);

            // Also fetch time series data for this platform
            const timeData = await metricsService.getPlatformTimeSeriesData(platform.id);
            setTimeSeriesData(timeData);

        } catch (error) {
            console.error("Failed to fetch platform metrics:", error);
            setError("Failed to load metrics. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    const fetchPlatformContent = async () => {
        try {
            const contentData = await metricsService.getPlatformContentList(platform.id);
            setContentList(contentData);
        } catch (error) {
            console.error("Failed to fetch platform content:", error);
        }
    };


    // Generate demo data if no real metrics exist
    const generateDemoMetrics = () => {
        return {
            totalViews: Math.floor(Math.random() * 50000),
            totalLikes: Math.floor(Math.random() * 5000),
            totalComments: Math.floor(Math.random() * 1000),
            engagementRate: (Math.random() * 8).toFixed(2),
            contentDistribution: [
                { name: 'Videos', value: Math.floor(Math.random() * 30) + 5 },
                { name: 'Posts', value: Math.floor(Math.random() * 20) + 3 },
                { name: 'Stories', value: Math.floor(Math.random() * 15) + 2 }
            ]
        };
    };

    const generateDemoTimeSeriesData = () => {
        const data = [];
        const today = new Date();

        for (let i = 30; i >= 0; i--) {
            const date = new Date();
            date.setDate(today.getDate() - i);
            data.push({
                date: date.toLocaleDateString(),
                views: Math.floor(Math.random() * 1000),
                likes: Math.floor(Math.random() * 100),
                comments: Math.floor(Math.random() * 50)
            });
        }

        return data;
    };

    if (!isOpen) return null;

    // Use demo data if no real metrics
    const displayMetrics = metrics || generateDemoMetrics();
    const displayTimeSeriesData = timeSeriesData.length > 0 ? timeSeriesData : generateDemoTimeSeriesData();

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
            <div className="bg-white rounded-lg shadow-xl w-11/12 max-w-4xl max-h-90vh overflow-y-auto p-6">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-2xl font-bold">{platform.platformName} Analytics</h2>
                    <button
                        onClick={onClose}
                        className="p-2 rounded-full hover:bg-gray-200 transition-colors"
                    >
                        <X size={24} />
                    </button>
                </div>

                {loading ? (
                    <div className="flex justify-center items-center h-64">
                        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                    </div>
                ) : error ? (
                    <div className="text-center text-red-500 p-4">{error}</div>
                ) : (
                    <div className="space-y-6">
                        {/* Metrics Overview */}
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                            <div className="bg-gray-50 p-4 rounded shadow">
                                <h3 className="text-sm font-medium text-gray-500">Total Views</h3>
                                <p className="text-2xl font-bold">{displayMetrics.totalViews.toLocaleString()}</p>
                            </div>
                            <div className="bg-gray-50 p-4 rounded shadow">
                                <h3 className="text-sm font-medium text-gray-500">Total Likes</h3>
                                <p className="text-2xl font-bold">{displayMetrics.totalLikes.toLocaleString()}</p>
                            </div>
                            <div className="bg-gray-50 p-4 rounded shadow">
                                <h3 className="text-sm font-medium text-gray-500">Total Comments</h3>
                                <p className="text-2xl font-bold">{displayMetrics.totalComments.toLocaleString()}</p>
                            </div>
                            <div className="bg-gray-50 p-4 rounded shadow">
                                <h3 className="text-sm font-medium text-gray-500">Engagement Rate</h3>
                                <p className="text-2xl font-bold">{displayMetrics.engagementRate}%</p>
                            </div>
                        </div>

                        {/* Charts Row */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {/* Content Distribution */}
                            <div className="bg-white border rounded-lg p-4 shadow-sm">
                                <h3 className="text-lg font-semibold mb-2">Content Distribution</h3>
                                <ResponsiveContainer width="100%" height={300}>
                                    <PieChart>
                                        <Pie
                                            data={displayMetrics.contentDistribution}
                                            cx="50%"
                                            cy="50%"
                                            labelLine={false}
                                            outerRadius={100}
                                            fill="#8884d8"
                                            dataKey="value"
                                            label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                                        >
                                            {displayMetrics.contentDistribution.map((entry, index) => (
                                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                            ))}
                                        </Pie>
                                        <Tooltip />
                                    </PieChart>
                                </ResponsiveContainer>
                            </div>

                            {/* Engagement Over Time */}
                            <div className="bg-white border rounded-lg p-4 shadow-sm">
                                <h3 className="text-lg font-semibold mb-2">Engagement Over Time</h3>
                                <ResponsiveContainer width="100%" height={300}>
                                    <LineChart data={displayTimeSeriesData}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="date" />
                                        <YAxis />
                                        <Tooltip />
                                        <Legend />
                                        <Line type="monotone" dataKey="views" stroke="#8884d8" name="Views" />
                                        <Line type="monotone" dataKey="likes" stroke="#82ca9d" name="Likes" />
                                        <Line type="monotone" dataKey="comments" stroke="#ffc658" name="Comments" />
                                    </LineChart>
                                </ResponsiveContainer>
                            </div>
                        </div>

                        {/* Top Performing Content */}
                        <div className="bg-white border rounded-lg p-4 shadow-sm">
                            <h3 className="text-lg font-semibold mb-4">Content List</h3>
                            <div className="overflow-y-auto max-h-60">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50 sticky top-0">
                                    <tr>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Title</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Views</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Likes</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Engagement</th>
                                    </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                    {contentList.length > 0 ? (
                                        contentList.map((content, index) => (
                                            <tr key={content.id}>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                                    {content.title}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    {content.type}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    {content.views?.toLocaleString() || 0}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    {content.likes?.toLocaleString() || 0}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    {content.engagementRate?.toFixed(1) || 0}%
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan="5" className="px-6 py-4 text-center text-sm text-gray-500">
                                                No content available for this platform
                                            </td>
                                        </tr>
                                    )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PlatformMetricsModal;