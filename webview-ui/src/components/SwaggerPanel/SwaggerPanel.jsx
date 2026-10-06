import React, { useState, useMemo } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faChevronRight, faChevronDown } from '@fortawesome/free-solid-svg-icons';
import SwaggerCard from './SwaggerCard';
import './SwaggerPanel.css';

export default function SwaggerPanel({ data, searchQuery = '', onEditApi }) {
    const { 
        endpoints = [], 
        colors = {}, 
        isDark = true, 
        requestResults = {}, 
        schemas = {},
        isEditorMode = false 
    } = data;

    const [collapsedGroups, setCollapsedGroups] = useState({});

    // Filter endpoints by query
    const filteredEndpoints = useMemo(() => {
        if (!searchQuery.trim()) return endpoints;
        const q = searchQuery.toLowerCase();
        return endpoints.filter(ep => 
            (ep.path && ep.path.toLowerCase().includes(q)) ||
            (ep.methodName && ep.methodName.toLowerCase().includes(q)) ||
            (ep.httpMethod && ep.httpMethod.toLowerCase().includes(q)) ||
            (ep.className && ep.className.toLowerCase().includes(q))
        );
    }, [endpoints, searchQuery]);

    // Group endpoints by interface/service class name
    const groupedEndpoints = useMemo(() => {
        const groups = {};
        filteredEndpoints.forEach(ep => {
            const key = ep.className || 'DefaultService';
            if (!groups[key]) groups[key] = [];
            groups[key].push(ep);
        });
        return groups;
    }, [filteredEndpoints]);

    const toggleGroup = (className) => {
        setCollapsedGroups(prev => ({
            ...prev,
            [className]: !prev[className]
        }));
    };

    return (
        <div className={`swagger-panel-container ${!isDark ? 'light' : ''}`}>
            {/* Endpoints List */}
            {Object.keys(groupedEndpoints).length === 0 ? (
                <div className="swagger-empty">
                    {searchQuery ? "No matching endpoints found." : "No API endpoints detected in this interface."}
                </div>
            ) : (
                Object.entries(groupedEndpoints).map(([className, serviceEndpoints]) => {
                    const hideGroupHeader = isEditorMode && Object.keys(groupedEndpoints).length <= 1;
                    const isCollapsed = !hideGroupHeader && !!collapsedGroups[className];
                    return (
                        <div key={className} className="swagger-service-group">
                            {!hideGroupHeader && (
                                <div 
                                    className="swagger-service-header"
                                    onClick={() => toggleGroup(className)}
                                >
                                    <div className="swagger-service-left">
                                        <span style={{ fontSize: '11px', color: '#8b949e', width: '12px', display: 'inline-flex', alignItems: 'center' }}>
                                            <FontAwesomeIcon icon={isCollapsed ? faChevronRight : faChevronDown} />
                                        </span>
                                        <span className="swagger-service-title">
                                            {className}
                                        </span>
                                    </div>
                                    <span className="swagger-service-count">
                                        {serviceEndpoints.length} endpoints
                                    </span>
                                </div>
                            )}

                            {!isCollapsed && (
                                <div className="swagger-service-cards" style={hideGroupHeader ? { marginTop: 0 } : undefined}>
                                    {serviceEndpoints.map(node => (
                                        <SwaggerCard 
                                            key={node.signature || `${node.className}.${node.methodName}`}
                                            node={node}
                                            methodColors={colors}
                                            isDark={isDark}
                                            responseText={requestResults[`${node.className}.${node.methodName}`]}
                                            schemaData={schemas}
                                            isEditorMode={isEditorMode}
                                            onEdit={onEditApi}
                                        />
                                    ))}
                                </div>
                            )}
                        </div>
                    );
                })
            )}
        </div>
    );
}
