// HomePage.jsx
import React from 'react';
import { Link } from 'react-router-dom';

const HomePage = () => {
    return (
        <div className="container mx-auto text-center py-10">
            <h1 className="text-4xl font-bold mb-6">Welcome to Content Analytics</h1>
            <p className="text-xl mb-8">
                Track and analyze your social media performance across multiple platforms
            </p>

            <div className="flex justify-center space-x-6">
                <Link
                    to="/dashboard"
                    className="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition"
                >
                    View Dashboard
                </Link>
                <Link
                    to="/platforms"
                    className="px-6 py-3 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition"
                >
                    Manage Platforms
                </Link>
            </div>
        </div>
    );
};

export default HomePage;