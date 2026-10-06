import React, { useState, useRef, useEffect } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
    faMagnifyingGlass, 
    faList, 
    faDiagramProject, 
    faPlus, 
    faXmark 
} from '@fortawesome/free-solid-svg-icons';
import './UnifiedHeader.css';

export default function UnifiedHeader({
    searchQuery,
    onSearchChange,
    endpointCount,
    totalCount,
    currentView,
    onViewChange,
    isEditorMode,
    onAddApi,
    isDark
}) {
    const [isSearchOpen, setIsSearchOpen] = useState(false);
    const searchInputRef = useRef(null);

    const handleToggleSearch = () => {
        if (isSearchOpen) {
            handleCloseSearch();
        } else {
            setIsSearchOpen(true);
            setTimeout(() => {
                searchInputRef.current?.focus();
                searchInputRef.current?.select();
            }, 60);
        }
    };

    const handleCloseSearch = () => {
        onSearchChange('');
        setIsSearchOpen(false);
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Escape') {
            handleCloseSearch();
        }
    };

    useEffect(() => {
        const handleGlobalKeyDown = (e) => {
            if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'f') {
                e.preventDefault();
                setIsSearchOpen(true);
                setTimeout(() => {
                    searchInputRef.current?.focus();
                    searchInputRef.current?.select();
                }, 60);
            } else if (e.key === 'Escape' && isSearchOpen) {
                handleCloseSearch();
            }
        };

        window.addEventListener('keydown', handleGlobalKeyDown);
        return () => window.removeEventListener('keydown', handleGlobalKeyDown);
    }, [isSearchOpen]);

    const displayTotal = totalCount !== undefined ? totalCount : endpointCount;

    return (
        <div className={`unified-header-wrapper ${!isDark ? 'light' : ''}`}>
            {/* Main Top Header Bar */}
            <header className="unified-header">
                {/* Left: Endpoint Badge */}
                <div className="header-left">
                    <div className="endpoint-count-badge" title="Total endpoints in service">
                        <span className="count-number">{displayTotal}</span>
                        <span className="count-label">{displayTotal === 1 ? 'endpoint' : 'endpoints'}</span>
                    </div>
                </div>

                {/* Right: Search Toggle Button + Segmented View Switcher + Add API CTA */}
                <div className="header-right">
                    {/* Search Icon Button */}
                    <button
                        className={`header-search-toggle-btn ${isSearchOpen ? 'active' : ''} ${searchQuery ? 'has-query' : ''}`}
                        onClick={handleToggleSearch}
                        title={isSearchOpen ? "Close search (Esc)" : "Search APIs (⌘F / Ctrl+F)"}
                    >
                        <FontAwesomeIcon icon={faMagnifyingGlass} style={{ fontSize: '13px' }} />
                        {searchQuery && !isSearchOpen && (
                            <span className="search-active-dot" />
                        )}
                    </button>

                    {/* View Switcher Segmented Control */}
                    <div className="segmented-control">
                        <button
                            className={`segmented-btn ${currentView === 'list' || currentView === 'swagger' ? 'active' : ''}`}
                            onClick={() => onViewChange('list')}
                            title="Card List View"
                        >
                            <span className="segmented-icon">
                                <FontAwesomeIcon icon={faList} />
                            </span>
                            <span className="segmented-text">List</span>
                        </button>
                        <button
                            className={`segmented-btn ${currentView === 'graph' ? 'active' : ''}`}
                            onClick={() => onViewChange('graph')}
                            title="Interactive Graph View"
                        >
                            <span className="segmented-icon">
                                <FontAwesomeIcon icon={faDiagramProject} />
                            </span>
                            <span className="segmented-text">Graph</span>
                        </button>
                    </div>

                    {/* Primary Add API Button (in editor mode) */}
                    {isEditorMode && onAddApi && (
                        <button
                            className="btn-add-api-primary"
                            onClick={onAddApi}
                            title="Add new API in this interface"
                        >
                            <span className="add-icon">
                                <FontAwesomeIcon icon={faPlus} />
                            </span>
                            <span className="add-text">Add API</span>
                        </button>
                    )}
                </div>
            </header>

            {/* Expandable Search Bar Drawer under the CTA bar */}
            {isSearchOpen && (
                <div className="unified-search-drawer">
                    <div className="search-drawer-inner">
                        <span className="search-drawer-icon">
                            <FontAwesomeIcon icon={faMagnifyingGlass} />
                        </span>
                        <input
                            ref={searchInputRef}
                            type="text"
                            className="search-drawer-input"
                            placeholder="Filter by HTTP method, path, or function name (e.g. GET, /products)..."
                            value={searchQuery}
                            onChange={(e) => onSearchChange(e.target.value)}
                            onKeyDown={handleKeyDown}
                        />

                        {searchQuery && (
                            <span className="search-results-pill">
                                {endpointCount} {endpointCount === 1 ? 'match' : 'matches'}
                            </span>
                        )}

                        <button
                            className="search-drawer-close"
                            onClick={handleCloseSearch}
                            title="Close search and reset (Esc)"
                        >
                            <FontAwesomeIcon icon={faXmark} />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
