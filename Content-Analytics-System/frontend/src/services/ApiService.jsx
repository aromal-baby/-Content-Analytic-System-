import axios from 'axios';

// Creating an axios instance
const api = axios.create({
    baseURL: 'http://localhost:8080/api',
});

// Adding a request instance
api.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        console.log(`Making ${config.method?.toUpperCase()} request to ${config.url} with token:`, token ? "Present" : "Not present");

        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        console.error("Request interceptor error:", error);
        return Promise.reject(error);
    }
);

// Response interceptor
api.interceptors.response.use(
    response => {
        return response;
    },
    error => {
        console.error("API error:",
            error.response?.status,
            error.response?.data || error.message);

        // Handle 401 response
        if (error.response && error.response.status === 401) {
            console.log("Unauthorized access - redirecting to login");
            // Clearing token and redirecting to login
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default api;