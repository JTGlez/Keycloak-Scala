import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import App from './App.jsx';
import { SecuredApp } from './SecuredApp.jsx';
import './index.css';
import 'bootstrap/dist/css/bootstrap.min.css';

ReactDOM.createRoot(document.getElementById('root')).render(
    <BrowserRouter>
      <React.Fragment>
        <div className="container">
          <ul>
            <li><Link to="/">Public Component</Link></li>
            <li><Link to="/secured">Secured Component</Link></li>
          </ul>
          <Routes>
            <Route path="/" element={<App />} />
            <Route path="/secured" element={<SecuredApp />} />
          </Routes>
        </div>
      </React.Fragment>
    </BrowserRouter>
);
