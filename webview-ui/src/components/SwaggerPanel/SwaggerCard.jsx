import React, { useState, useEffect } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
    faPen, 
    faCheck, 
    faCopy, 
    faArrowUpRightFromSquare, 
    faChevronDown, 
    faPlay, 
    faXmark 
} from '@fortawesome/free-solid-svg-icons';
import { KotlinBridge } from '../../api/KotlinBridge';
import './SwaggerCard.css';

export default function SwaggerCard({ node, methodColors, isDark, responseText, schemaData, isEditorMode, onEdit }) {
    const [isExpanded, setIsExpanded] = useState(false);
    const [paramValues, setParamValues] = useState({});
    const [bodyText, setBodyText] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [copied, setCopied] = useState(false);
    const [localResponse, setLocalResponse] = useState(null);

    const colors = methodColors[node.httpMethod] || { badge: '#6e7681', border: '#4e5157' };
    const methodColor = colors.badge || '#58a6ff';

    // Sync response from Kotlin when received
    useEffect(() => {
        if (responseText) {
            setLocalResponse(responseText);
            setIsLoading(false);
        }
    }, [responseText]);

    // Format path to highlight {variable}
    const renderFormattedPath = (path) => {
        if (!path) return '/';
        const parts = path.split(/(\{.*?\})/g);
        return parts.map((part, i) => {
            if (part.startsWith('{') && part.endsWith('}')) {
                return <span key={i} className="swagger-path-var">{part}</span>;
            }
            return part;
        });
    };

    const handleCopyPath = (e) => {
        e.stopPropagation();
        KotlinBridge.copyToClipboard(node.path);
        setCopied(true);
        setTimeout(() => setCopied(false), 1500);
    };

    const handleNavigate = (e) => {
        e.stopPropagation();
        KotlinBridge.navigateToSource(node.signature);
    };

    const handleParamChange = (name, value) => {
        setParamValues(prev => ({ ...prev, [name]: value }));
    };

    const handleExecute = (e) => {
        e.stopPropagation();
        setIsLoading(true);

        let finalUrl = node.path;
        const queryParts = [];

        (node.parameters || []).forEach(param => {
            const val = paramValues[param.name];
            if (param.location === 'PATH' && val !== undefined) {
                finalUrl = finalUrl.replace(`{${param.name}}`, encodeURIComponent(val));
            } else if (param.location === 'QUERY' && val !== undefined && val.trim() !== '') {
                queryParts.push(`${encodeURIComponent(param.name)}=${encodeURIComponent(val)}`);
            }
        });

        if (queryParts.length > 0) {
            const delimiter = finalUrl.includes('?') ? '&' : '?';
            finalUrl += delimiter + queryParts.join('&');
        }

        const bodyPayload = bodyText.trim() ? bodyText : null;
        KotlinBridge.executeApiCall(node.signature, finalUrl, bodyPayload);
    };

    // Body schema pre-fill
    const bodyParam = (node.parameters || []).find(p => p.location === 'BODY');
    const schemaJson = bodyParam?.fqn ? schemaData[bodyParam.fqn] : null;

    useEffect(() => {
        if (isExpanded && bodyParam?.fqn && !schemaJson) {
            KotlinBridge.requestSchema(bodyParam.fqn);
        }
    }, [isExpanded, bodyParam]);

    return (
        <div 
            className={`swagger-card ${!isDark ? 'light' : ''}`}
            style={{ '--method-color': methodColor }}
        >
            <div className="swagger-card-stripe" />
            
            <div 
                className="swagger-card-header"
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <div className="swagger-header-top-row">
                    <div className="swagger-function-col">
                        <span className="swagger-function-name">
                            {node.methodName}
                        </span>
                    </div>

                    <div className="swagger-header-actions">
                        {isEditorMode && (
                            <button 
                                className="swagger-icon-btn"
                                title="Edit API in Editor"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onEdit && onEdit(node);
                                }}
                            >
                                <FontAwesomeIcon icon={faPen} />
                            </button>
                        )}
                        <button 
                            className="swagger-icon-btn"
                            title={copied ? "Copied!" : "Copy Path"}
                            onClick={handleCopyPath}
                        >
                            {copied ? <FontAwesomeIcon icon={faCheck} /> : <FontAwesomeIcon icon={faCopy} />}
                        </button>
                        <button 
                            className="swagger-icon-btn"
                            title="Navigate to Source"
                            onClick={handleNavigate}
                        >
                            <FontAwesomeIcon icon={faArrowUpRightFromSquare} />
                        </button>
                        <span className={`swagger-chevron ${isExpanded ? 'open' : ''}`}>
                            <FontAwesomeIcon icon={faChevronDown} />
                        </span>
                    </div>
                </div>

                <div className="swagger-card-divider" />

                <div className="swagger-header-bottom-row">
                    <span 
                        className="swagger-badge"
                        style={{ backgroundColor: methodColor }}
                    >
                        {node.httpMethod}
                    </span>
                    <span className="swagger-path">
                        {renderFormattedPath(node.path)}
                    </span>
                </div>
            </div>

            {isExpanded && (
                <div className="swagger-interactive">
                    <div className="swagger-section-title">
                        Parameters ({(node.parameters || []).length})
                    </div>

                    {(node.parameters || []).length === 0 ? (
                        <div style={{ fontSize: '11px', fontStyle: 'italic', color: '#8b949e', marginBottom: '10px' }}>
                            No parameters required for this endpoint.
                        </div>
                    ) : (
                        <div className="swagger-param-list">
                            {node.parameters.map((param, idx) => (
                                <div key={idx} className="swagger-param-row">
                                    <div className="swagger-param-meta">
                                        <span className="swagger-param-name">{param.name}</span>
                                        <span className={`swagger-param-loc ${(param.location || '').toLowerCase()}`}>
                                            {param.location}
                                        </span>
                                        <span 
                                            className="swagger-param-type"
                                            title={`Navigate to ${param.type}`}
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                KotlinBridge.navigateToType(param.fqn || param.type, node.className);
                                            }}
                                        >
                                            {param.type}
                                        </span>
                                    </div>
                                    <input 
                                        type="text"
                                        className="swagger-param-input"
                                        placeholder={`Value for ${param.name}`}
                                        value={paramValues[param.name] || ''}
                                        onChange={(e) => handleParamChange(param.name, e.target.value)}
                                        onClick={(e) => e.stopPropagation()}
                                    />
                                    {param.location === 'BODY' && (
                                        <div className="swagger-schema-container" onClick={(e) => e.stopPropagation()}>
                                            <div className="swagger-schema-header">
                                                <span>Request Body (JSON)</span>
                                                {schemaJson && (
                                                    <button 
                                                        className="swagger-btn-small"
                                                        onClick={() => setBodyText(schemaJson)}
                                                    >
                                                        Use Sample
                                                    </button>
                                                )}
                                            </div>
                                            <textarea 
                                                className="swagger-schema-textarea"
                                                placeholder={schemaJson || "Enter JSON body..."}
                                                value={bodyText}
                                                onChange={(e) => setBodyText(e.target.value)}
                                            />
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}

                    <div className="swagger-actions-row">
                        <button 
                            className="swagger-execute-btn"
                            disabled={isLoading}
                            onClick={handleExecute}
                        >
                            {isLoading ? 'Sending...' : (
                                <>
                                    <FontAwesomeIcon icon={faPlay} style={{ marginRight: '6px', fontSize: '11px' }} />
                                    Execute
                                </>
                            )}
                        </button>
                    </div>

                    {(localResponse || isLoading) && (
                        <div className="swagger-response-terminal">
                            <div className="swagger-response-header">
                                <span>RESPONSE</span>
                                <div style={{ display: 'flex', gap: '6px' }}>
                                    <button 
                                        className="swagger-icon-btn"
                                        title="Copy Response"
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            if (localResponse) KotlinBridge.copyToClipboard(localResponse);
                                        }}
                                    >
                                        <FontAwesomeIcon icon={faCopy} />
                                    </button>
                                    <button 
                                        className="swagger-icon-btn"
                                        title="Clear Response"
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            setLocalResponse(null);
                                        }}
                                    >
                                        <FontAwesomeIcon icon={faXmark} />
                                    </button>
                                </div>
                            </div>
                            <pre className="swagger-response-content">
                                {isLoading ? "Sending request..." : localResponse}
                            </pre>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
