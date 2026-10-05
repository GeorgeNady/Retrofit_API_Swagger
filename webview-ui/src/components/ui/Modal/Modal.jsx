import React, { useEffect } from 'react';
import './Modal.css';

/**
 * Pure reusable modal dialog primitive.
 * Zero domain logic, zero platform/backend dependencies.
 */
export default function Modal({
    isOpen,
    onClose,
    title,
    children,
    isDark = true,
    width = '520px',
    className = ''
}) {
    useEffect(() => {
        if (!isOpen) return;
        const handleKeyDown = (e) => {
            if (e.key === 'Escape') {
                onClose?.();
            }
        };
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [isOpen, onClose]);

    if (!isOpen) return null;

    return (
        <div className="ui-modal-overlay" onClick={onClose}>
            <div
                className={`ui-modal-card ${!isDark ? 'light' : ''} ${className}`}
                style={{ width }}
                onClick={(e) => e.stopPropagation()}
            >
                {title && (
                    <div className="ui-modal-header">
                        <span className="ui-modal-title">{title}</span>
                        <button
                            type="button"
                            className="ui-modal-close-btn"
                            onClick={onClose}
                            aria-label="Close dialog"
                        >
                            ✕
                        </button>
                    </div>
                )}
                <div className="ui-modal-body">
                    {children}
                </div>
            </div>
        </div>
    );
}
