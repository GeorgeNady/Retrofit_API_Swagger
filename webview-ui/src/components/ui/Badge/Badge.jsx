import React from 'react';
import './Badge.css';

/**
 * Pure presentation badge primitive.
 */
export default function Badge({
    children,
    variant = 'neutral',
    className = '',
    style = {}
}) {
    const normalizedVariant = (variant || 'neutral').toLowerCase();
    return (
        <span
            className={`ui-badge ui-badge-${normalizedVariant} ${className}`}
            style={style}
        >
            {children}
        </span>
    );
}
