import React from 'react';
import MethodLabel from '../MethodLabel/MethodLabel';
import './ApiNode.css';
import { THEME_COLORS } from '../../config/cyStyles.js'

const ApiNode = ({ data }) => {
    const colors = data.methodColors || { badge: '#888' };

    // Style logic matching previous implementation
    const borderStyle = data.supportsCache ? `3px solid ${THEME_COLORS.supportCacheColor}` :
                       data.isInvalidator ? `3px solid ${THEME_COLORS.invalidateCacheColor}` :
                       '1px solid #4e5157';

    return (
        <div className="node-card" style={{ border: borderStyle }}>
            <span className="method-name">{data.label}</span>
            <div className="node-header">
                <MethodLabel
                    method={data.method}
                    color={colors.badge}
                />
                <div className="node-path">{data.path}</div>
            </div>
        </div>
    );
};

export default ApiNode;
