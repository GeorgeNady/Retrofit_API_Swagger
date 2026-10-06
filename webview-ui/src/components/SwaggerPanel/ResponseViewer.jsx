import React, { useState, useMemo, useEffect, useRef } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
    faCopy, 
    faCheck, 
    faXmark, 
    faCode, 
    faEye, 
    faFileLines, 
    faAlignLeft,
    faMagnifyingGlass 
} from '@fortawesome/free-solid-svg-icons';
import { KotlinBridge } from '../../api/KotlinBridge';
import {
    detectFormat,
    formatJson,
    formatXml,
    highlightJson,
    highlightXml,
    highlightYaml,
    formatByteSize,
    escapeHtml
} from '../../utils/responseFormatter';
import './ResponseViewer.css';

export default function ResponseViewer({ responseText, isDark, onClear }) {
    const [mode, setMode] = useState('pretty'); // 'pretty' | 'raw' | 'preview'
    const [format, setFormat] = useState('json'); // 'json' | 'xml' | 'html' | 'yaml' | 'text'
    const [isWrapped, setIsWrapped] = useState(false);
    const [copied, setCopied] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [isSearchOpen, setIsSearchOpen] = useState(false);
    const searchInputRef = useRef(null);

    // Auto-detect format whenever a new responseText arrives
    useEffect(() => {
        if (responseText) {
            const detected = detectFormat(responseText);
            setFormat(detected);
            setMode('pretty');
        }
    }, [responseText]);

    useEffect(() => {
        if (isSearchOpen && searchInputRef.current) {
            searchInputRef.current.focus();
        }
    }, [isSearchOpen]);

    // Calculate byte size
    const byteSize = useMemo(() => {
        if (!responseText) return 0;
        return new Blob([responseText]).size;
    }, [responseText]);

    // Formatted code based on active format
    const formattedCode = useMemo(() => {
        if (!responseText) return '';
        if (format === 'json') return formatJson(responseText);
        if (format === 'xml' || format === 'html') return formatXml(responseText);
        return responseText;
    }, [responseText, format]);

    // Rendered lines for display
    const { lines, matchCount } = useMemo(() => {
        const textToDisplay = mode === 'raw' ? responseText : formattedCode;
        if (!textToDisplay) return { lines: [], matchCount: 0 };

        let highlightedText;
        if (mode === 'raw') {
            highlightedText = escapeHtml(textToDisplay);
        } else {
            switch (format) {
                case 'json':
                    highlightedText = highlightJson(textToDisplay);
                    break;
                case 'xml':
                case 'html':
                    highlightedText = highlightXml(textToDisplay);
                    break;
                case 'yaml':
                    highlightedText = highlightYaml(textToDisplay);
                    break;
                default:
                    highlightedText = escapeHtml(textToDisplay);
            }
        }

        let rawLines = highlightedText.split('\n');
        let count = 0;

        if (searchQuery.trim()) {
            const escapedQuery = escapeHtml(searchQuery.trim());
            // Highlight search query without breaking existing HTML tags
            const searchRegex = new RegExp(`(${escapedQuery.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})(?![^<]*>|[^<>]*<\\/)`, 'gi');
            rawLines = rawLines.map(line => {
                const matches = line.match(searchRegex);
                if (matches) count += matches.length;
                return line.replace(searchRegex, '<mark class="swagger-search-mark">$1</mark>');
            });
        }

        return { lines: rawLines, matchCount: count };
    }, [formattedCode, responseText, mode, format, searchQuery]);

    const handleCopy = (e) => {
        e.stopPropagation();
        const textToCopy = mode === 'raw' ? responseText : formattedCode;
        KotlinBridge.copyToClipboard(textToCopy);
        setCopied(true);
        setTimeout(() => setCopied(false), 1500);
    };

    const isHtmlOrSvg = format === 'html' || (format === 'xml' && responseText?.includes('<svg'));

    return (
        <div className={`swagger-response-terminal ${!isDark ? 'light' : ''} format-${format}`}>
            {/* Top Toolbar */}
            <div className="swagger-response-header">
                {/* Left: Format badge & metadata */}
                <div className="swagger-response-meta-left">
                    <span className="swagger-response-title">RESPONSE</span>
                    <span className={`swagger-format-badge format-${format}`}>
                        {format.toUpperCase()}
                    </span>
                    <span className="swagger-meta-pill" title="Payload size">
                        {formatByteSize(byteSize)}
                    </span>
                    <span className="swagger-meta-pill" title="Total line count">
                        {lines.length} {lines.length === 1 ? 'line' : 'lines'}
                    </span>
                </div>

                {/* Center: Mode switcher (Pretty / Raw / Preview) & Format selector */}
                <div className="swagger-response-controls">
                    <div className="swagger-segmented-group">
                        <button 
                            className={`swagger-seg-btn ${mode === 'pretty' ? 'active' : ''}`}
                            onClick={() => setMode('pretty')}
                            title="Pretty formatted with syntax highlighting"
                        >
                            <FontAwesomeIcon icon={faCode} style={{ marginRight: '4px', fontSize: '10px' }} />
                            Pretty
                        </button>
                        <button 
                            className={`swagger-seg-btn ${mode === 'raw' ? 'active' : ''}`}
                            onClick={() => setMode('raw')}
                            title="Unformatted raw response text"
                        >
                            <FontAwesomeIcon icon={faFileLines} style={{ marginRight: '4px', fontSize: '10px' }} />
                            Raw
                        </button>
                        {isHtmlOrSvg && (
                            <button 
                                className={`swagger-seg-btn ${mode === 'preview' ? 'active' : ''}`}
                                onClick={() => setMode('preview')}
                                title="Interactive rendered preview"
                            >
                                <FontAwesomeIcon icon={faEye} style={{ marginRight: '4px', fontSize: '10px' }} />
                                Preview
                            </button>
                        )}
                    </div>

                    {mode !== 'preview' && (
                        <div className="swagger-format-select-group">
                            {['json', 'xml', 'html', 'yaml', 'text'].map(fmt => (
                                <button
                                    key={fmt}
                                    className={`swagger-format-btn btn-${fmt} ${format === fmt ? 'active' : ''}`}
                                    onClick={() => setFormat(fmt)}
                                    title={`View as ${fmt.toUpperCase()}`}
                                >
                                    {fmt.toUpperCase()}
                                </button>
                            ))}
                        </div>
                    )}
                </div>

                {/* Right: Actions */}
                <div className="swagger-response-actions">
                    {mode !== 'preview' && (
                        <>
                            <button
                                className={`swagger-icon-btn ${isSearchOpen ? 'active' : ''}`}
                                title={isSearchOpen ? "Close search" : "Search in response"}
                                onClick={() => {
                                    setIsSearchOpen(!isSearchOpen);
                                    if (isSearchOpen) setSearchQuery('');
                                }}
                            >
                                <FontAwesomeIcon icon={faMagnifyingGlass} />
                            </button>
                            <button
                                className={`swagger-icon-btn ${isWrapped ? 'active' : ''}`}
                                title={isWrapped ? "Disable line wrapping" : "Enable line wrapping"}
                                onClick={() => setIsWrapped(!isWrapped)}
                            >
                                <FontAwesomeIcon icon={faAlignLeft} />
                            </button>
                        </>
                    )}
                    <button 
                        className="swagger-icon-btn"
                        title={copied ? "Copied to clipboard!" : "Copy response"}
                        onClick={handleCopy}
                    >
                        {copied ? <FontAwesomeIcon icon={faCheck} style={{ color: '#3fb950' }} /> : <FontAwesomeIcon icon={faCopy} />}
                    </button>
                    {onClear && (
                        <button 
                            className="swagger-icon-btn"
                            title="Clear response"
                            onClick={(e) => {
                                e.stopPropagation();
                                onClear();
                            }}
                        >
                            <FontAwesomeIcon icon={faXmark} />
                        </button>
                    )}
                </div>
            </div>

            {/* Quick Search Bar */}
            {isSearchOpen && mode !== 'preview' && (
                <div className="swagger-response-search-bar">
                    <FontAwesomeIcon icon={faMagnifyingGlass} className="search-bar-icon" />
                    <input
                        ref={searchInputRef}
                        type="text"
                        placeholder="Search within response..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="swagger-search-input"
                    />
                    {searchQuery && (
                        <span className="swagger-search-count">
                            {matchCount} {matchCount === 1 ? 'match' : 'matches'}
                        </span>
                    )}
                    <button 
                        className="swagger-search-close-btn"
                        onClick={() => {
                            setSearchQuery('');
                            setIsSearchOpen(false);
                        }}
                    >
                        <FontAwesomeIcon icon={faXmark} />
                    </button>
                </div>
            )}

            {/* Response Content Viewer */}
            <div className={`swagger-response-body ${isWrapped ? 'wrap-lines' : ''}`}>
                {mode === 'preview' && isHtmlOrSvg ? (
                    <div className="swagger-preview-container">
                        <iframe
                            title="HTML Preview"
                            srcDoc={responseText}
                            sandbox="allow-same-origin"
                            className="swagger-html-iframe"
                        />
                    </div>
                ) : (
                    <div className="swagger-code-viewer">
                        {/* Line Numbers Gutter */}
                        <div className="swagger-gutter">
                            {lines.map((_, idx) => (
                                <div key={idx} className="swagger-line-number">
                                    {idx + 1}
                                </div>
                            ))}
                        </div>

                        {/* Code Lines */}
                        <div className="swagger-code-lines">
                            {lines.map((lineHtml, idx) => (
                                <div 
                                    key={idx} 
                                    className="swagger-code-line"
                                    dangerouslySetInnerHTML={{ __html: lineHtml || '&nbsp;' }}
                                />
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
