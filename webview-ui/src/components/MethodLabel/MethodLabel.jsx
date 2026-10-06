import React from 'react';
import './MethodLabel.css';

const MethodLabel = ({ method, color }) => {
    return (
        <span
            className="method-badge"
            style={{ backgroundColor: color || '#888' }}
        >
            {method}
        </span>
    );
};

export default MethodLabel;
