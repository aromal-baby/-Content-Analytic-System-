import axios from 'axios';
import api from './ApiService';

const API_URL = 'http://localhost:8080/api/auth';

class AuthService {
    async login(username, password) {
        try {
            console.log("Attempting login for user:", username);
            const response = await axios.post(`${API_URL}/login`, {username, password});

            if (response.data && response.data.token) {
                const token = response.data.token;
                console.log("Login successful, token received:", token.substring(0, 10) + "...");
                localStorage.setItem('token', token);
                console.log("Token saved to localStorage");

                // Wait a moment to ensure token is saved
                await new Promise(resolve => setTimeout(resolve, 100));

                // Verify token was saved
                const savedToken = localStorage.getItem('token');
                console.log("Verified token in storage:", savedToken ? "Present" : "Missing",
                    savedToken ? savedToken.substring(0, 10) + "..." : "");
            } else {
                console.error("No token in login response:", response.data);
            }

            return response.data;
        } catch (error) {
            console.error("Login failed:", error.response?.data || error.message);
            throw error;
        }
    }

    async register(userData) {
        console.log("Sending registration data:", userData);
        try {
            const response = await axios.post(`${API_URL}/register`, userData);
            console.log("Registration response:", response.data);
            return response.data
        } catch (error) {
            console.error("Registration error", error);
            throw error;
        }
    }

    async getCurrentUser() {
        try {
            console.log("Fetching current user data");
            const token = this.getToken();

            if (!token) {
                console.log("No token found");
                throw new Error("Not authenticated");
            }

            // Use axios with the token in the header
            const response = await axios.get(`${API_URL}/me`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            console.log("User data fetched successfully");
            return response.data;
        } catch (error) {
            console.error("Failed to get current user", error.response?.data || error.message);
            throw error;
        }
    }

    getToken() {
        const token = localStorage.getItem('token');
        console.log("Retrieved token from localStorage:", token ? "Present" : "Not found");
        return token;    }

    async logout() {
        try {
            // Calling backend logout endpoint
            await axios.post(`${API_URL}/logout`);
        } catch (error) {
            console.error("Error during logout API call:", error);
            // Continuing with logout even if API call fails
        } finally {
            // Clearing the local storage
            console.log("Removing token from localStorage");
            localStorage.removeItem('token');
        }
    }

    isAuthenticated() {
        return !!this.getToken();
    }
}

export default new AuthService();