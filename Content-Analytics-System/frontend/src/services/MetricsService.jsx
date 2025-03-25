import api from './ApiService';

class MetricsService {
    async getMetricsSummary() {
        try {
            const response = await api.get('/metrics/summary');
            return response.data;
        } catch (error) {
            console.error('Error fetching metrics summary:', error);
            return {
                totalViews: 0,
                totalLikes: 0,
                totalComments: 0,
                averageEngagementRate: 0.0
            };
        }
    }

    async getTimeSeriesData() {
        try {
            const response = await api.get('/metrics/timeseries');
            return response.data;
        } catch (error) {
            console.error('Error fetching time series data:', error);
            return [];
        }
    }

    async getContentTimeSeriesData(contentId) {
        try {
            const response = await api.get(`/metrics/content/${contentId}/timeseries`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching time series data for content ${contentId}:`, error);
            return [];
        }
    }

    async getMetricsByPlatform() {
        try {
            const response = await api.get('/metrics/by-platform');
            return response.data;
        } catch (error) {
            console.error('Error fetching platform metrics:', error);
            return [];
        }
    }

    // New method to get metrics for a specific platform
    async getPlatformSpecificMetrics(platformId) {
        try {
            const response = await api.get(`/metrics/platform/${platformId}`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching metrics for platform ${platformId}:`, error);
            return null;
        }
    }

    // New method for time series data for a specific platform
    async getPlatformTimeSeriesData(platformId) {
        try {
            const response = await api.get(`/metrics/platform/${platformId}/timeseries`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching time series data for platform ${platformId}:`, error);
            return [];
        }
    }

    async refreshContentMetrics(contentId) {
        try {
            await api.post(`/metrics/refresh/${contentId}`);
            return true;
        } catch (error) {
            console.error('Error refreshing metrics:', error);
            return false;
        }
    }

    // Add this to your MetricsService.jsx
    async getPlatformContentList(platformId) {
        try {
            const response = await api.get(`/metrics/platform/${platformId}/contents`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching content list for platform ${platformId}:`, error);
            return [];
        }
    }

    // For refreshing the metrics real-time
    async refreshPlatformMetrics(platformId) {
        try {
            const response = await api.post(`/metrics/platform/${platformId}/refresh`);
            return response.data;
        } catch (error) {
            console.error(`Error refreshing metrics for platform ${platformId}:`, error);
            throw error;
        }
    }
}

export default new MetricsService();