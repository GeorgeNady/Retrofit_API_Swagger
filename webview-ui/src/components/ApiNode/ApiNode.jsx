import React from 'react';
import MethodLabel from '../MethodLabel/MethodLabel';
import './ApiNode.css';

const ApiNode = ({ data }) => {
    const colors = data.methodColors || { badge: '#888' };

    return (
        <div className="node-card">
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
