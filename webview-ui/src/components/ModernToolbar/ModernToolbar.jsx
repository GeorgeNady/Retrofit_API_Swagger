import React from 'react';
import './ModernToolbar.css';

const SvgIcon = ({ d, size = 18 }) => (
    <svg
        width={size}
        height={size}
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
    >
        <path d={d} />
    </svg>
);

const Icons = {
    Pointer: "m3 3 7.07 16.97 2.51-7.39 7.39-2.51L3 3zM13 13l6 6",
    Hand: "M18 11c0-.55-.45-1-1-1s-1 .45-1 1v3.38l-1.37-.35L14 13.86V4c0-.55-.45-1-1-1s-1 .45-1 1v10.38l-1.37-.35L10 13.86V5c0-.55-.45-1-1-1s-1 .45-1 1v9.38l-1.37-.35L6 13.86V7c0-.55-.45-1-1-1s-1 .45-1 1v10c0 3.31 2.69 6 6 6h4c3.31 0 6-2.69 6-6v-6z",
    ZoomIn: "m21 21-4.35-4.35M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16zM11 8v6M8 11h6",
    ZoomOut: "m21 21-4.35-4.35M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16zM8 11h6",
    Focus: "M3 7V5a2 2 0 0 1 2-2h2m10 0h2a2 2 0 0 1 2 2v2m0 10v2a2 2 0 0 1-2 2h-2M7 21H5a2 2 0 0 1-2-2v-2"
};

const ModernToolbar = ({ 
    isPanMode, 
    onTogglePan, 
    onZoomIn, 
    onZoomOut, 
    onZoomReset, 
    onZoomFit
}) => {
    return (
        <div className="modern-toolbar">
            <div className="toolbar-group">
                <button
                    className={`toolbar-btn ${!isPanMode ? 'active' : ''}`}
                    onClick={() => isPanMode && onTogglePan()}
                    title="Select Mode"
                >
                    <SvgIcon d={Icons.Pointer} />
                </button>
                <button
                    className={`toolbar-btn ${isPanMode ? 'active' : ''}`}
                    onClick={() => !isPanMode && onTogglePan()}
                    title="Pan Mode"
                >
                    <SvgIcon d={Icons.Hand} />
                </button>
            </div>

            <div className="toolbar-divider" />

            <div className="toolbar-group">
                <button className="toolbar-btn" onClick={onZoomIn} title="Zoom In">
                    <SvgIcon d={Icons.ZoomIn} />
                </button>
                <button className="toolbar-btn" onClick={onZoomOut} title="Zoom Out">
                    <SvgIcon d={Icons.ZoomOut} />
                </button>
                <button className="toolbar-btn text-btn" onClick={onZoomReset} title="Actual Size (1:1)">
                    1:1
                </button>
                <button className="toolbar-btn" onClick={onZoomFit} title="Fit to Screen">
                    <SvgIcon d={Icons.Focus} />
                </button>
            </div>
        </div>
    );
};

export default ModernToolbar;
