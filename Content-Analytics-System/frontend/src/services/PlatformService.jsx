import api from './ApiService';

class PlatformService {
    constructor() {
        // Use environment variable with fallback
        //this.baseURL = 'http://localhost:8080/api';
        this.platformsURL = '/platforms';
        this.contentURL = '/content';
    }

    // Platform methods
    async getUserPlatforms() {
        try {
            const response = await api.get(this.platformsURL);
            return response.data;
        } catch (error) {
            console.error('Error fetching platforms:', error);
            throw error;
        }
    }

    async addPlatform(platformData) {
        try {
            console.log("Adding platform with data:", platformData);
            const response = await api.post(this.platformsURL, platformData);
            console.log("Platform creation response:", response);

            // Extracting ID..
            let platformId = null;
            if (typeof response.data === 'string') {
                console.log("Response data is a string, attempting to extract ID with regex");
                const idMatch = response.data.match(/"id"\s*:\s*(\d+)/);
                if (idMatch && idMatch[1]) {
                    platformId = parseInt(idMatch[1], 10);
                    console.log("Successfully extracted ID with regex:", platformId);
                }
            } else if (response.data && response.data.id) {
                platformId = response.data.id;
                console.log("Found ID directly in response object:", platformId);
            }

            return {
                success: true,
                data: { id: platformId },
                status: response.status
            };
        } catch (error) {
            console.error('Error adding platform:', error);
            return {
                success: false,
                error: error.message,
                status: error.response?.status
            };
        }
    }

    // Content methods
    async addContent(contentData) {
        try {
            console.log("Sending content data to server:", contentData);
            const response = await api.post(this.contentURL, contentData);
            console.log("Content addition response:", response);
            return response.data;
        } catch (error) {
            console.error('Error adding content:', error);
            if (error.response) {
                console.error('Error response data:', error.response.data);
                console.error('Error response status:', error.response.status);
                console.error('Error response headers:', error.response.headers);
            } else if (error.request) {
                console.error('No response received, request was:', error.request);
            } else {
                console.error('Error setting up request:', error.message);
            }
            throw error;
        }
    }

    async getContentByPlatform(platformId) {
        try {
            const response = await api.get(`${this.platformsURL}/${platformId}/content`);
            return response.data;
        } catch (error) {
            console.error('Error fetching content:', error);
            throw error;
        }
    }

    async getPlatformStats() {
        try {
            const response = await api.get(`${this.platformsURL}/stats`);
            return response.data;
        } catch (error) {
            console.error('Error fetching platform statistics:', error);
            return {}; // Return empty object as fallback
        }
    }
}

export default new PlatformService();