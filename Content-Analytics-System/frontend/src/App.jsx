import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext'; // Import ThemeProvider
import PrivateRoute from './components/PrivateRoute';
import Layout from './components/Layout/Layout';
import HomePage from './components/Home/HomePage';
import DashboardPage from './components/Dashboard/DashboardPage';
import PlatformsPage from './pages/PlatformsPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

function App() {
    return (
        <AuthProvider>
            <ThemeProvider>
                <Router>
                    <Routes>
                        {/* Public routes */}
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />

                        {/* Protected routes */}
                        <Route element={<PrivateRoute />}>
                            <Route path="/" element={
                                <Layout>
                                    <HomePage />
                                </Layout>
                            } />
                            <Route path="/dashboard" element={
                                <Layout>
                                    <DashboardPage />
                                </Layout>
                            } />
                            <Route path="/platforms" element={
                                <Layout>
                                    <PlatformsPage />
                                </Layout>
                            } />
                        </Route>

                        {/* Fallback */}
                        <Route path="*" element={<Navigate to="/" />} />
                    </Routes>
                </Router>
            </ThemeProvider>
        </AuthProvider>
    );
}

export default App;