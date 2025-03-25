import React, { createContext, useState, useEffect, useContext } from 'react';
import authService from '../services/AuthService';
import axios from 'axios'; // Import axios for direct API calls if needed

const AuthContext = createContext();

export const useAuth = () => {
    return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadUser = async () => {
            try {
                const token = localStorage.getItem('token');
                console.log("Token from localStorage:", token ? "Present" : "Not found");

                if (token) {
                    const userData = await authService.getCurrentUser();
                    console.log("User data loaded:", userData);
                    setUser(userData);
                }
            } catch (err) {
                console.error('Failed to load user:', err);
                // Clearing invalid token
                localStorage.removeItem('token');
            } finally {
                setLoading(false)
            }
        };

        loadUser();
    }, []);

    const login = async (username, password) => {
        try {
            setError(null);
            console.log("Attempting login for user:", username);

            // Using authService for login
            const data = await authService.login(username, password);
            console.log("Login response:", data);

            if (data.token) {
                localStorage.setItem('token', data.token);
                console.log("Token saved:", data.token);

                if (data.user) {
                    console.log("User data from login:", data.user);
                    setUser(data.user);
                } else {
                    console.log("No user data in login response, fetching separately");
                    const userData = await authService.getCurrentUser();
                    setUser(userData);
                }

                return data.user;
            } else {
                console.error("No token in login response");
                throw new Error("Login failed: No authentication token received");
            }
        } catch (err) {
            console.error('Login error:', err);
            setError(err.response?.data?.message || 'Login failed');
            throw err;
        }
    };

    const register = async (userData) => {
        console.log("AuthContext register called with:", userData);
        try {
            setError(null);
            console.log("Registration successful:");
            return await authService.register(userData);

        } catch (err) {
            console.log("Registration failed:", err);
            setError(err.response?.data?.message || 'Registration failed')
            throw err;
        }
    };

    const logout = () => {
        console.log("Logging out user");
        localStorage.removeItem('token')
        setUser(null)
    };

    const value = {
        user,
        loading,
        error,
        login,
        register,
        logout,
        isAuthenticated: !!user
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export default AuthContext;