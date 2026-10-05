import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
    faArrowRight, 
    faChevronRight, 
    faBolt, 
    faDatabase, 
    faLink 
} from '@fortawesome/free-solid-svg-icons';
import Modal from '../ui/Modal/Modal';
import Button from '../ui/Button/Button';
import Badge from '../ui/Badge/Badge';
import './EdgeActionDialog.css';

/**
 * Feature component for selecting an edge action when dragging a connection.
 * Composes generic Modal, Button, and Badge UI primitives.
 */
export default function EdgeActionDialog({
    connection,
    edgeActions = [],
    onSelectAction,
    onClose,
    isDark = true
}) {
    if (!connection) return null;

    const { source, target } = connection;

    const renderActionIcon = (icon) => {
        switch (icon) {
            case '⚡':
            case 'bolt':
                return <FontAwesomeIcon icon={faBolt} style={{ color: '#e3b341' }} />;
            case '💾':
            case 'database':
                return <FontAwesomeIcon icon={faDatabase} style={{ color: '#58a6ff' }} />;
            case '🔗':
            case 'link':
                return <FontAwesomeIcon icon={faLink} style={{ color: '#bc8cff' }} />;
            default:
                return <FontAwesomeIcon icon={faBolt} style={{ color: '#e3b341' }} />;
        }
    };

    // Fallback actions if none provided
    const actions = edgeActions.length > 0 ? edgeActions : [
        {
            id: 'cache_invalidation',
            name: 'Setup Cache Invalidation',
            description: 'Annotate target with @InvalidateCache pointing to source cache key',
            icon: 'bolt',
            placement: 'ANNOTATE_TARGET'
        },
        {
            id: 'support_cache',
            name: 'Mark Source as Cached',
            description: 'Annotate source with @SupportCache',
            icon: 'database',
            placement: 'ANNOTATE_SOURCE'
        },
        {
            id: 'flow_chain_stub',
            name: 'Generate Flow Chaining Comment',
            description: 'Injects a reactive Coroutine Flow pipeline comment above the target API',
            icon: 'link',
            placement: 'INJECT_BEFORE_TARGET'
        }
    ];

    const formatPlacement = (placement) => {
        switch (placement) {
            case 'ANNOTATE_TARGET': return 'Target Annotation';
            case 'ANNOTATE_SOURCE': return 'Source Annotation';
            case 'INJECT_BEFORE_TARGET': return 'Inject Code';
            case 'GENERATE_NEW_FILE': return 'New File';
            default: return placement;
        }
    };

    return (
        <Modal
            isOpen={!!connection}
            onClose={onClose}
            title="Connect APIs — Edge Action"
            isDark={isDark}
            width="490px"
        >
            <div className="edge-connection-flow">
                <div className="edge-node-pill">
                    <Badge variant={source.httpMethod || 'neutral'}>
                        {source.httpMethod || 'API'}
                    </Badge>
                    <span className="edge-node-name">{source.label || source.methodName}</span>
                </div>
                <span className="edge-flow-arrow">
                    <FontAwesomeIcon icon={faArrowRight} />
                </span>
                <div className="edge-node-pill">
                    <Badge variant={target.httpMethod || 'neutral'}>
                        {target.httpMethod || 'API'}
                    </Badge>
                    <span className="edge-node-name">{target.label || target.methodName}</span>
                </div>
            </div>

            <div className="edge-actions-list">
                <div className="edge-actions-prompt">
                    Select an action or template to execute:
                </div>

                {actions.map((action) => (
                    <div
                        key={action.id}
                        className="edge-action-card"
                        onClick={() => onSelectAction(action.id)}
                    >
                        <div className="edge-action-icon">{renderActionIcon(action.icon)}</div>
                        <div className="edge-action-info">
                            <div className="edge-action-name-row">
                                <span className="edge-action-name">{action.name}</span>
                                {action.placement && (
                                    <Badge variant="info">
                                        {formatPlacement(action.placement)}
                                    </Badge>
                                )}
                            </div>
                            {action.description && (
                                <div className="edge-action-desc">{action.description}</div>
                            )}
                        </div>
                        <span className="edge-action-arrow">
                            <FontAwesomeIcon icon={faChevronRight} style={{ fontSize: '14px' }} />
                        </span>
                    </div>
                ))}
            </div>

            <div className="edge-dialog-footer">
                <Button variant="secondary" onClick={onClose}>
                    Cancel
                </Button>
            </div>
        </Modal>
    );
}
