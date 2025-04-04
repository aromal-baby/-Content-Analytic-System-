import React from 'react';
import ReactDOM from 'react-dom/client';
import './assets/css/style.css';  // Updated the import path
import App from './App';
import reportWebVitals from './reportWebVitals';

// Add bootstrap to package.json if you need it
import 'bootstrap/dist/css/bootstrap.min.css';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();