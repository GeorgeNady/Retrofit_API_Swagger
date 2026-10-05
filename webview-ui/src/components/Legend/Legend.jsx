import React from 'react';
import './Legend.css';

const Legend = () => {
    return (
        <div className="legend">
            <div className="legend-item">
                <div className="legend-color support-color" />
                <span>@SupportCache</span>
            </div>
            <div className="legend-item">
                <div className="legend-color invalidate-color" />
                <span>@InvalidateCache</span>
            </div>
        </div>
    );
};

export default Legend;
