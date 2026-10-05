import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
    faArrowPointer, 
    faHand, 
    faMagnifyingGlassPlus, 
    faMagnifyingGlassMinus, 
    faExpand 
} from '@fortawesome/free-solid-svg-icons';
import './ModernToolbar.css';

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
                    <FontAwesomeIcon icon={faArrowPointer} />
                </button>
                <button
                    className={`toolbar-btn ${isPanMode ? 'active' : ''}`}
                    onClick={() => !isPanMode && onTogglePan()}
                    title="Pan Mode"
                >
                    <FontAwesomeIcon icon={faHand} />
                </button>
            </div>

            <div className="toolbar-divider" />

            <div className="toolbar-group">
                <button className="toolbar-btn" onClick={onZoomIn} title="Zoom In">
                    <FontAwesomeIcon icon={faMagnifyingGlassPlus} />
                </button>
                <button className="toolbar-btn" onClick={onZoomOut} title="Zoom Out">
                    <FontAwesomeIcon icon={faMagnifyingGlassMinus} />
                </button>
                <button className="toolbar-btn text-btn" onClick={onZoomReset} title="Actual Size (1:1)">
                    1:1
                </button>
                <button className="toolbar-btn" onClick={onZoomFit} title="Fit to Screen">
                    <FontAwesomeIcon icon={faExpand} />
                </button>
            </div>
        </div>
    );
};

export default ModernToolbar;
