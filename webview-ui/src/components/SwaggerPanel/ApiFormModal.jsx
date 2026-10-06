import React, { useState, useEffect } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faTrashCan } from '@fortawesome/free-solid-svg-icons';
import Modal from '../ui/Modal/Modal';
import Button from '../ui/Button/Button';
import './ApiFormModal.css';

const HTTP_METHODS = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'];
const PARAM_LOCATIONS = ['PATH', 'QUERY', 'HEADER', 'BODY'];

/**
 * Feature form modal for creating or editing API endpoints.
 * Completely decoupled from backend/IPC (invokes onSubmit callback).
 * Composes generic Modal and Button UI primitives.
 */
export default function ApiFormModal({ isOpen, onClose, initialData, isDark, onSubmit }) {
    const isUpdate = !!initialData;

    const [httpMethod, setHttpMethod] = useState('GET');
    const [path, setPath] = useState('');
    const [methodName, setMethodName] = useState('');
    const [returnType, setReturnType] = useState('Unit');
    const [isSuspend, setIsSuspend] = useState(true);
    const [parameters, setParameters] = useState([]);
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        if (initialData) {
            setHttpMethod(initialData.httpMethod || 'GET');
            setPath(initialData.path || '');
            setMethodName(initialData.methodName || '');
            setReturnType(initialData.returnType || 'Unit');
            setIsSuspend(initialData.isSuspend !== undefined ? initialData.isSuspend : true);
            setParameters(
                (initialData.parameters || []).map((p, idx) => ({
                    id: idx,
                    name: p.name || '',
                    location: p.location || 'QUERY',
                    type: p.type || 'String'
                }))
            );
        } else {
            setHttpMethod('GET');
            setPath('');
            setMethodName('');
            setReturnType('Unit');
            setIsSuspend(true);
            setParameters([]);
        }
        setErrorMessage('');
    }, [initialData, isOpen]);

    if (!isOpen) return null;

    const handleAddParam = () => {
        setParameters(prev => [
            ...prev,
            { id: Date.now(), name: '', location: 'QUERY', type: 'String' }
        ]);
    };

    const handleRemoveParam = (id) => {
        setParameters(prev => prev.filter(p => p.id !== id));
    };

    const handleParamChange = (id, field, value) => {
        setParameters(prev => prev.map(p => p.id === id ? { ...p, [field]: value } : p));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!path.trim()) {
            setErrorMessage('Path cannot be empty.');
            return;
        }
        if (!methodName.trim()) {
            setErrorMessage('Method name cannot be empty.');
            return;
        }

        const payload = {
            isUpdate,
            originalSignature: initialData?.signature || null,
            httpMethod,
            path: path.trim(),
            methodName: methodName.trim(),
            returnType: returnType.trim() || 'Unit',
            isSuspend,
            parameters: parameters.map(({ name, location, type }) => ({
                name: name.trim(),
                location: location.trim(),
                type: type.trim() || 'String'
            }))
        };

        if (onSubmit) {
            onSubmit(payload);
        }
        onClose();
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={isUpdate ? 'Edit API Endpoint' : 'Create New API Endpoint'}
            isDark={isDark}
            width="580px"
        >
            <form onSubmit={handleSubmit} className="api-modal-form-content">
                {errorMessage && (
                    <div style={{ color: '#f85149', fontSize: '12px', marginBottom: '10px' }}>
                        {errorMessage}
                    </div>
                )}

                <div className="api-form-row">
                    <div className="api-form-group" style={{ flex: '0 0 110px' }}>
                        <label className="api-form-label">HTTP Method</label>
                        <select 
                            className="api-form-select"
                            value={httpMethod}
                            onChange={(e) => setHttpMethod(e.target.value)}
                        >
                            {HTTP_METHODS.map(m => (
                                <option key={m} value={m}>{m}</option>
                            ))}
                        </select>
                    </div>

                    <div className="api-form-group">
                        <label className="api-form-label">Endpoint Path</label>
                        <input 
                            type="text"
                            className="api-form-input"
                            placeholder="e.g. users/{id} or auth/login"
                            value={path}
                            onChange={(e) => setPath(e.target.value)}
                        />
                    </div>
                </div>

                <div className="api-form-row">
                    <div className="api-form-group">
                        <label className="api-form-label">Kotlin Function Name</label>
                        <input 
                            type="text"
                            className="api-form-input"
                            placeholder="e.g. getUserProfile"
                            value={methodName}
                            onChange={(e) => setMethodName(e.target.value)}
                        />
                    </div>

                    <div className="api-form-group">
                        <label className="api-form-label">Return Type</label>
                        <input 
                            type="text"
                            className="api-form-input"
                            placeholder="e.g. UserResponse or Response<User>"
                            value={returnType}
                            onChange={(e) => setReturnType(e.target.value)}
                        />
                    </div>
                </div>

                <label className="api-form-checkbox-label">
                    <input 
                        type="checkbox"
                        checked={isSuspend}
                        onChange={(e) => setIsSuspend(e.target.checked)}
                    />
                    <span>Kotlin Coroutines (suspend function)</span>
                </label>

                {/* Parameters Section */}
                <div className="api-params-section">
                    <div className="api-params-header">
                        <span className="api-form-label">Parameters ({parameters.length})</span>
                        <Button
                            variant="secondary"
                            onClick={handleAddParam}
                            style={{ padding: '3px 8px', fontSize: '11px' }}
                        >
                            <FontAwesomeIcon icon={faPlus} style={{ marginRight: '5px' }} />
                            Add Parameter
                        </Button>
                    </div>

                    {parameters.length === 0 ? (
                        <div style={{ fontSize: '11px', color: '#8b949e', fontStyle: 'italic' }}>
                            No parameters specified.
                        </div>
                    ) : (
                        <div className="api-params-list">
                            {parameters.map((param) => (
                                <div key={param.id} className="api-param-item">
                                    <select 
                                        className="api-form-select"
                                        style={{ width: '90px' }}
                                        value={param.location}
                                        onChange={(e) => handleParamChange(param.id, 'location', e.target.value)}
                                    >
                                        {PARAM_LOCATIONS.map(loc => (
                                             <option key={loc} value={loc}>{loc}</option>
                                        ))}
                                    </select>
                                    <input 
                                        type="text"
                                        className="api-form-input"
                                        placeholder="Parameter name"
                                        value={param.name}
                                        onChange={(e) => handleParamChange(param.id, 'name', e.target.value)}
                                    />
                                    <input 
                                        type="text"
                                        className="api-form-input"
                                        placeholder="Type (e.g. String, Int)"
                                        value={param.type}
                                        onChange={(e) => handleParamChange(param.id, 'type', e.target.value)}
                                    />
                                    <button 
                                        type="button"
                                        className="api-param-remove-btn"
                                        onClick={() => handleRemoveParam(param.id)}
                                        title="Remove parameter"
                                    >
                                        <FontAwesomeIcon icon={faTrashCan} />
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                <div className="api-modal-footer">
                    <Button variant="secondary" onClick={onClose}>
                        Cancel
                    </Button>
                    <Button variant="primary" type="submit">
                        {isUpdate ? 'Save Changes' : 'Create API'}
                    </Button>
                </div>
            </form>
        </Modal>
    );
}
