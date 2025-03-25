import React, { useState, useEffect } from 'react';
import api from '../../services/ApiService';
import metricsService from '../../services/MetricsService';
import {
    LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
    XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import { Mail, DollarSign, UserPlus, Activity, ArrowUp, DownloadCloud } from 'lucide-react';

const DashboardPage = () => {
    const [dashboardData, setDashboardData] = useState(null);
    const [metricsData, setMetricsData] = useState(null);
    const [timeSeriesData, setTimeSeriesData] = useState([]);
    const [platformMetrics, setPlatformMetrics] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Define colors for charts
    const COLORS = ['#3b82f6', '#10b981', '#6366f1', '#f59e0b', '#ef4444'];
    const GRADIENTS = ['#3CBBEC', '#34D399'];

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);

                // Fetch all data in parallel
                const [dashboardResponse, metricsResponse, timeSeriesResponse, platformResponse] =
                    await Promise.all([
                        api.get('/dashboard/summary'),
                        metricsService.getMetricsSummary(),
                        metricsService.getTimeSeriesData(),
                        metricsService.getMetricsByPlatform()
                    ]);

                console.log("Dashboard data:", dashboardResponse.data);
                console.log("Metrics data:", metricsResponse);
                console.log("Time series data:", timeSeriesResponse);
                console.log("Platform metrics:", platformResponse);

                setDashboardData(dashboardResponse.data);
                setMetricsData(metricsResponse);
                setTimeSeriesData(timeSeriesResponse || []);
                setPlatformMetrics(platformResponse || []);
            } catch (err) {
                console.error("Error loading dashboard:", err);
                setError('Failed to load dashboard data');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    if (loading) return (
        <div className="flex justify-center items-center h-96">
            <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-blue-500"></div>
        </div>
    );

    if (error) return <div className="text-center p-10 text-red-500">{error}</div>;
    if (!dashboardData) return <div className="text-center p-10 text-gray-400">No dashboard data available</div>;

    // Derive data for platform distribution chart
    const platformData = (() => {
        try {
            const distribution = dashboardData.platforms?.platformDistribution || {};
            const formattedData = Object.entries(distribution).map(
                ([name, value]) => ({ name, value: Number(value) })
            );
            return formattedData.length > 0 ? formattedData : [{ name: 'No Platforms', value: 1 }];
        } catch (err) {
            console.error("Error formatting platform data:", err);
            return [{ name: 'No Platforms', value: 1 }];
        }
    })();

    // Derive data for content type distribution
    const contentTypeData = (() => {
        try {
            const distribution = dashboardData.content?.contentTypeDistribution || {};
            const formattedData = Object.entries(distribution).map(
                ([name, value]) => ({
                    name: name.replace(/_/g, ' ').toLowerCase()
                        .split(' ')
                        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
                        .join(' '),
                    value: Number(value)
                })
            );
            return formattedData.length > 0 ? formattedData : [{ name: 'No Content', value: 1 }];
        } catch (err) {
            console.error("Error formatting content type data:", err);
            return [{ name: 'No Content', value: 1 }];
        }
    })();

    return (
        <div className="space-y-6">
            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div className="bg-[#1a1e2e] rounded-lg shadow-lg p-6 border border-gray-700/20">
                    <div className="flex justify-between items-start">
                        <div>
                            <Mail className="text-teal-400 mb-3" size={20} />
                            <h2 className="text-3xl font-bold text-white">{dashboardData?.platforms?.totalPlatforms || 0}</h2>
                            <p className="text-gray-400 text-sm">Connected Platforms</p>
                        </div>
                        <div className="w-12 h-12">
                            <svg viewBox="0 0 36 36" className="circular-chart">
                                <path className="circle-bg"
                                      d="M18 2.0845
                                    a 15.9155 15.9155 0 0 1 0 31.831
                                    a 15.9155 15.9155 0 0 1 0 -31.831"
                                      fill="none"
                                      stroke="#2c3444"
                                      strokeWidth="4"
                                />
                                <path className="circle"
                                      d="M18 2.0845
                                    a 15.9155 15.9155 0 0 1 0 31.831
                                    a 15.9155 15.9155 0 0 1 0 -31.831"
                                      fill="none"
                                      stroke="url(#gradient1)"
                                      strokeWidth="4"
                                      strokeDasharray={`${(dashboardData?.platforms?.totalPlatforms || 0) * 20}, 100`}
                                />
                                <defs>
                                    <linearGradient id="gradient1" x1="0%" y1="0%" x2="100%" y2="0%">
                                        <stop offset="0%" stopColor="#3CBBEC" />
                                        <stop offset="100%" stopColor="#34D399" />
                                    </linearGradient>
                                </defs>
                            </svg>
                        </div>
                    </div>
                    <div className="flex items-center mt-2">
                        <ArrowUp className="text-green-400 mr-1" size={14} />
                        <span className="text-green-400 text-xs">+24%</span>
                    </div>
                </div>

                <div className="bg-[#1a1e2e] rounded-lg shadow-lg p-6 border border-gray-700/20">
                    <div className="flex justify-between items-start">
                        <div>
                            <DollarSign className="text-blue-400 mb-3" size={20} />
                            <h2 className="text-3xl font-bold text-white">{dashboardData?.content?.totalContents || 0}</h2>
                            <p className="text-gray-400 text-sm">Total Content</p>
                        </div>
                        <div className="w-12 h-12">
                            <svg viewBox="0 0 36 36" className="circular-chart">
                                <path className="circle-bg"
                                      d="M18 2.0845
                                    a 15.9155 15.9155 0 0 1 0 31.831
                                    a 15.9155 15.9155 0 0 1 0 -31.831"
                                      fill="none"
                                      stroke="#2c3444"
                                      strokeWidth="4"
                                />
                                <path className="circle"
                                      d="M18 2.0845
                                    a 15.9155 15.9155 0 0 1 0 31.831
                                    a 15.9155 15.9155 0 0 1 0 -31.831"
                                      fill="none"
                                      stroke="url(#gradient2)"
                                      strokeWidth="4"
                                      strokeDasharray={`${(dashboardData?.content?.totalContents || 0) * 5}, 100`}
                                />
                                <defs>
                                    <linearGradient id="gradient2" x1="0%" y1="0%" x2="100%" y2="0%">
                                        <stop offset="0%" stopColor="#818cf8" />
                                        <stop offset="100%" stopColor="#3b82f6" />
                                    </linearGradient>
                                </defs>
                            </svg>
                        </div>
                    </div>
                    <div className="flex items-center mt-2">
                        <ArrowUp className="text-green-400 mr-1" size={14} />
                        <span className="text-green-400 text-xs">+21%</span>
                    </div>
                </div>

                <div className="bg-[#1a1e2e] rounded-lg shadow-lg p-6 border border-gray-700/20">
                    <div className="flex justify-between items-start">
                        <div>
                            <UserPlus className="text-indigo-400 mb-3" size={20} />
                            <h2 className="text-3xl font-bold text-white">{(metricsData?.totalViews || 0).toLocaleString()}</h2>
                            <p className="text-gray-400 text-sm">Total Views</p>
                        </div>
                        <div className="w-12 h-12">
                            <svg viewBox="0 0 36 36" className="circular-chart">
                                <path className="circle-bg"
                                      d="M18 2.0845
                                    a 15.9155 15.9155 0 0 1 0 31.831
                                    a 15.9155 15.9155 0 0 1 0 -31.831"
                                      fill="none"
                                      stroke="#2c3444"
                                      strokeWidth="4"
                                />
                                <path className="circle"
                                      d="M18 2.0845
                                    a 15.9155 15.9155 0 0 1 0 31.831
                                    a 15.9155 15.9155 0 0 1 0 -31.831"
                                      fill="none"
                                      stroke="url(#gradient3)"
                                      strokeWidth="4"
                                      strokeDasharray="65, 100"
                                />
                                <defs>
                                    <linearGradient id="gradient3" x1="0%" y1="0%" x2="100%" y2="0%">
                                        <stop offset="0%" stopColor="#6366f1" />
                                        <stop offset="100%" stopColor="#a855f7" />
                                    </linearGradient>
                                </defs>
                            </svg>
                        </div>
                    </div>
                    <div className="flex items-center mt-2">
                        <ArrowUp className="text-green-400 mr-1" size={14} />
                        <span className="text-green-400 text-xs">+5%</span>
                    </div>
                </div>

                <div className="bg-[#1a1e2e] rounded-lg shadow-lg p-6 border border-gray-700/20">
                    <div className="flex justify-between items-start">
                        <div>
                            <Activity className="text-teal-400 mb-3" size={20} />
                            <h2 className="text-3xl font-bold text-white">{(metricsData?.averageEngagementRate || 0).toFixed(2)}%</h2>
                            <p className="text-gray-400 text-sm">Engagement Rate</p>
                        </div>
                        <div className="w-12 h-12">
                            <svg viewBox="0 0 36 36" className="circular-chart">
                                <path className="circle-bg"
                                      d="M18 2.0845
                                    a 15.9155 15.9155 0 0 1 0 31.831
                                    a 15.9155 15.9155 0 0 1 0 -31.831"
                                      fill="none"
                                      stroke="#2c3444"
                                      strokeWidth="4"
                                />
                                <path className="circle"
                                      d="M18 2.0845
                                    a 15.9155 15.9155 0 0 1 0 31.831
                                    a 15.9155 15.9155 0 0 1 0 -31.831"
                                      fill="none"
                                      stroke="url(#gradient4)"
                                      strokeWidth="4"
                                      strokeDasharray="78, 100"
                                />
                                <defs>
                                    <linearGradient id="gradient4" x1="0%" y1="0%" x2="100%" y2="0%">
                                        <stop offset="0%" stopColor="#10b981" />
                                        <stop offset="100%" stopColor="#34d399" />
                                    </linearGradient>
                                </defs>
                            </svg>
                        </div>
                    </div>
                    <div className="flex items-center mt-2">
                        <ArrowUp className="text-green-400 mr-1" size={14} />
                        <span className="text-green-400 text-xs">+28%</span>
                    </div>
                </div>
            </div>

            {/* Main Chart Row */}
            <div className="bg-[#1a1e2e] rounded-lg shadow-lg p-6 border border-gray-700/20">
                <div className="flex justify-between items-center mb-6">
                    <div>
                        <h2 className="text-xl font-semibold text-white">Engagement Over Time</h2>
                        <p className="text-gray-400 text-sm">Views, Likes, and Comments</p>
                    </div>
                    <button className="text-gray-400 hover:text-white">
                        <DownloadCloud size={20} />
                    </button>
                </div>
                <ResponsiveContainer width="100%" height={350}>
                    <LineChart data={timeSeriesData}>
                        <defs>
                            <linearGradient id="colorViews" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="5%" stopColor="#8884d8" stopOpacity={0.8}/>
                                <stop offset="95%" stopColor="#8884d8" stopOpacity={0}/>
                            </linearGradient>
                            <linearGradient id="colorLikes" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="5%" stopColor="#82ca9d" stopOpacity={0.8}/>
                                <stop offset="95%" stopColor="#82ca9d" stopOpacity={0}/>
                            </linearGradient>
                            <linearGradient id="colorComments" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="5%" stopColor="#ffc658" stopOpacity={0.8}/>
                                <stop offset="95%" stopColor="#ffc658" stopOpacity={0}/>
                            </linearGradient>
                        </defs>
                        <CartesianGrid strokeDasharray="3 3" stroke="#2c3444" />
                        <XAxis dataKey="date" stroke="#6b7280" />
                        <YAxis stroke="#6b7280" />
                        <Tooltip
                            contentStyle={{
                                backgroundColor: '#262f40',
                                borderColor: '#374151',
                                color: '#fff'
                            }}
                            labelStyle={{ color: '#fff' }}
                        />
                        <Legend wrapperStyle={{ color: '#fff' }} />
                        <Line
                            type="monotone"
                            dataKey="views"
                            stroke="#8884d8"
                            strokeWidth={2}
                            activeDot={{ r: 8 }}
                            name="Views"
                        />
                        <Line
                            type="monotone"
                            dataKey="likes"
                            stroke="#82ca9d"
                            strokeWidth={2}
                            name="Likes"
                        />
                        <Line
                            type="monotone"
                            dataKey="comments"
                            stroke="#ffc658"
                            strokeWidth={2}
                            name="Comments"
                        />
                    </LineChart>
                </ResponsiveContainer>
            </div>

            {/* Charts Row */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {/* Platform Distribution */}
                <div className="bg-[#1a1e2e] rounded-lg shadow-lg p-6 border border-gray-700/20">
                    <h2 className="text-xl font-semibold mb-4 text-white">Platform Distribution</h2>
                    <div className="h-64 flex items-center justify-center">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={platformData}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={80}
                                    paddingAngle={5}
                                    dataKey="value"
                                >
                                    {platformData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip
                                    contentStyle={{
                                        backgroundColor: '#262f40',
                                        borderColor: '#374151',
                                        color: '#fff'
                                    }}
                                    labelStyle={{ color: '#fff' }}
                                />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                    <div className="text-center mt-2 text-gray-500 text-sm">
                        {platformData[0].name === 'No Platforms'
                            ? 'No platforms connected yet'
                            : `${platformData.length} platforms connected`}
                    </div>
                </div>

                {/* Platform Performance */}
                <div className="bg-[#1a1e2e] rounded-lg shadow-lg p-6 border border-gray-700/20">
                    <h2 className="text-xl font-semibold mb-4 text-white">Platform Performance</h2>
                    {platformMetrics.length > 0 ? (
                        <ResponsiveContainer width="100%" height={250}>
                            <BarChart data={platformMetrics}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#2c3444" />
                                <XAxis dataKey="platform" stroke="#6b7280" />
                                <YAxis stroke="#6b7280" />
                                <Tooltip
                                    contentStyle={{
                                        backgroundColor: '#262f40',
                                        borderColor: '#374151',
                                        color: '#fff'
                                    }}
                                    labelStyle={{ color: '#fff' }}
                                />
                                <Bar
                                    dataKey="engagementRate"
                                    name="Engagement Rate %"
                                    radius={[4, 4, 0, 0]}
                                >
                                    {platformMetrics.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    ) : (
                        <div className="flex items-center justify-center h-64 text-gray-400">
                            No platform performance data available
                        </div>
                    )}
                </div>

                {/* Content Type Distribution */}
                <div className="bg-[#1a1e2e] rounded-lg shadow-lg p-6 border border-gray-700/20">
                    <h2 className="text-xl font-semibold mb-4 text-white">Content Type Distribution</h2>
                    <ResponsiveContainer width="100%" height={250}>
                        <BarChart data={contentTypeData} layout="vertical">
                            <CartesianGrid strokeDasharray="3 3" stroke="#2c3444" />
                            <XAxis type="number" stroke="#6b7280" />
                            <YAxis dataKey="name" type="category" stroke="#6b7280" />
                            <Tooltip
                                contentStyle={{
                                    backgroundColor: '#262f40',
                                    borderColor: '#374151',
                                    color: '#fff'
                                }}
                                labelStyle={{ color: '#fff' }}
                            />
                            <Bar
                                dataKey="value"
                                fill="#8884d8"
                                name="Content Count"
                                radius={[0, 4, 4, 0]}
                            >
                                {contentTypeData.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {/* Recent Content Section */}
            <div className="bg-[#1a1e2e] rounded-lg shadow-lg p-6 border border-gray-700/20">
                <h2 className="text-xl font-semibold mb-4 text-white">Recent Content</h2>
                {dashboardData?.content?.recentContent?.length > 0 ? (
                    <div className="overflow-x-auto">
                        <table className="min-w-full">
                            <thead>
                            <tr className="border-b border-gray-700">
                                <th className="py-3 px-4 text-left text-sm font-medium text-gray-400">Title</th>
                                <th className="py-3 px-4 text-left text-sm font-medium text-gray-400">Platform</th>
                                <th className="py-3 px-4 text-left text-sm font-medium text-gray-400">Type</th>
                                <th className="py-3 px-4 text-left text-sm font-medium text-gray-400">Published</th>
                                <th className="py-3 px-4 text-right text-sm font-medium text-gray-400">Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            {dashboardData.content.recentContent.map((content, index) => (
                                <tr key={index} className="border-b border-gray-700/50">
                                    <td className="py-3 px-4 text-white">{content.title}</td>
                                    <td className="py-3 px-4 text-gray-300">
                                        {typeof content.platform === 'object' && content.platform !== null
                                            ? content.platform.platformName
                                            : typeof content.platform === 'number'
                                                ? `Platform ID: ${content.platform}`
                                                : 'Unknown Platform'}
                                    </td>
                                    <td className="py-3 px-4 text-gray-300">{content.contentType}</td>
                                    <td className="py-3 px-4 text-gray-300">
                                        {content.publishedDate
                                            ? new Date(content.publishedDate).toLocaleDateString()
                                            : 'Not Published'}
                                    </td>
                                    <td className="py-3 px-4 text-right">
                                        <button
                                            onClick={() => metricsService.refreshContentMetrics(content.id)}
                                            className="bg-gradient-to-r from-blue-600 to-teal-500 text-white px-3 py-1 rounded text-xs font-medium"
                                        >
                                            Refresh Metrics
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                ) : (
                    <p className="text-gray-400">No recent content available</p>
                )}
            </div>
        </div>
    );
};

export default DashboardPage;