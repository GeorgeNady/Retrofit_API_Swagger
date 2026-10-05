import React from 'react';
import './Button.css';

/**
 * Pure reusable button primitive.
 * Zero business logic.
 */
export default function Button({
    children,
    onClick,
    variant = 'secondary',
    type = 'button',
    disabled = false,
    className = '',
    title = '',
    style = {}
}) {
    return (
        <button
            type={type}
            onClick={onClick}
            disabled={disabled}
            className={`ui-btn ui-btn-${variant} ${className}`}
            title={title}
            style={style}
        >
            {children}
        </button>
    );
}
