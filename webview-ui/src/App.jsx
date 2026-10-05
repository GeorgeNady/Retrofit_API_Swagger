import React, { useState, useRef, useEffect, useMemo } from 'react';
import { useKotlinData } from './hooks/useKotlinData';
import { useCytoscape } from './hooks/useCytoscape.js';
import UnifiedHeader from './components/UnifiedHeader/UnifiedHeader';
import Toolbar from './components/ModernToolbar/ModernToolbar';
import Legend from './components/Legend/Legend';
import SwaggerPanel from './components/SwaggerPanel/SwaggerPanel';
import ApiFormModal from './components/SwaggerPanel/ApiFormModal';
import { KotlinBridge } from './api/KotlinBridge';

function App() {
  const containerRef = useRef(null);
  const [isPanMode, setIsPanMode] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingNode, setEditingNode] = useState(null);

  // URL query params: ?view=graph|list|swagger &editor=true|false
  const params = new URLSearchParams(window.location.search);
  const queryParam = params.get('view');
  const isEditor = params.get('editor') === 'true';

  // Default mode:
  // - If specified in URL (e.g. ?view=graph or ?view=list): respect it
  // - Else: editor mode defaults to 'list', plugin window defaults to 'graph'
  const initialView = queryParam
    ? (queryParam === 'graph' ? 'graph' : 'list')
    : (isEditor ? 'list' : 'graph');

  const [currentView, setCurrentView] = useState(initialView);

  const graphState = useKotlinData();
  const cyRef = useCytoscape(containerRef, graphState, isPanMode);

  // Filter endpoints for count and for passing down
  const filteredEndpoints = useMemo(() => {
    const list = graphState.endpoints || [];
    if (!searchQuery.trim()) return list;
    const q = searchQuery.toLowerCase();
    return list.filter(ep => 
      (ep.path && ep.path.toLowerCase().includes(q)) ||
      (ep.methodName && ep.methodName.toLowerCase().includes(q)) ||
      (ep.httpMethod && ep.httpMethod.toLowerCase().includes(q)) ||
      (ep.className && ep.className.toLowerCase().includes(q))
    );
  }, [graphState.endpoints, searchQuery]);

  // Synchronize Graph node highlighting when searching
  useEffect(() => {
    if (!cyRef.current) return;
    const cy = cyRef.current;
    if (!searchQuery.trim()) {
      cy.elements().removeClass('dimmed').removeClass('highlighted');
      return;
    }
    const q = searchQuery.toLowerCase();
    cy.batch(() => {
      cy.elements().forEach(ele => {
        if (ele.isNode() && ele.data('type') === 'method') {
          const path = (ele.data('path') || '').toLowerCase();
          const label = (ele.data('label') || '').toLowerCase();
          const method = (ele.data('httpMethod') || '').toLowerCase();
          const matches = path.includes(q) || label.includes(q) || method.includes(q);
          if (matches) {
            ele.removeClass('dimmed').addClass('highlighted');
          } else {
            ele.removeClass('highlighted').addClass('dimmed');
          }
        }
      });
    });
  }, [searchQuery, cyRef]);

  const handleViewChange = (mode) => {
    const normalized = mode === 'graph' ? 'graph' : 'list';
    setCurrentView(normalized);
    if (normalized === 'graph' && cyRef.current) {
      setTimeout(() => {
        cyRef.current.resize();
        cyRef.current.fit(null, 50);
      }, 60);
    }
    KotlinBridge.send('switchViewMode', { mode: normalized });
  };

  useEffect(() => {
    window.setViewMode = (mode) => {
      const normalized = mode === 'graph' ? 'graph' : 'list';
      setCurrentView(normalized);
      if (normalized === 'graph' && cyRef.current) {
        setTimeout(() => {
          cyRef.current.resize();
          cyRef.current.fit(null, 50);
        }, 60);
      }
    };
  }, []);

  const handleAddApi = () => {
    setEditingNode(null);
    setIsModalOpen(true);
  };

  const handleEditApi = (node) => {
    setEditingNode(node);
    setIsModalOpen(true);
  };

  return (
    <div style={{ width: '100%', height: '100%', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
      {/* Unified Top Command Header */}
      <UnifiedHeader
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        endpointCount={filteredEndpoints.length}
        currentView={currentView}
        onViewChange={handleViewChange}
        isEditorMode={isEditor || graphState.isEditorMode}
        onAddApi={handleAddApi}
        isDark={graphState.isDark}
      />

      {/* Main Content Area */}
      <div style={{ flex: 1, position: 'relative', overflow: 'hidden' }}>
        {/* The Graph Canvas */}
        <div
          style={{
            width: '100%',
            height: '100%',
            position: 'absolute',
            top: 0,
            left: 0,
            visibility: currentView === 'graph' ? 'visible' : 'hidden',
            pointerEvents: currentView === 'graph' ? 'auto' : 'none'
          }}
        >
          <div
            ref={containerRef}
            id="cy"
            style={{ width: '100%', height: '100%' }}
          />

          <Toolbar
            isPanMode={isPanMode}
            onTogglePan={() => setIsPanMode(!isPanMode)}
            onZoomIn={() => cyRef.current?.zoom(cyRef.current.zoom() * 1.2)}
            onZoomOut={() => cyRef.current?.zoom(cyRef.current.zoom() * 0.8)}
            onZoomReset={() => {
              if (cyRef.current) {
                cyRef.current.zoom(1);
                cyRef.current.center();
              }
            }}
            onZoomFit={() => cyRef.current?.fit(null, 50)}
          />

          <Legend />
        </div>

        {/* List / Swagger View */}
        <div
          style={{
            width: '100%',
            height: '100%',
            position: 'absolute',
            top: 0,
            left: 0,
            visibility: currentView === 'list' ? 'visible' : 'hidden',
            pointerEvents: currentView === 'list' ? 'auto' : 'none',
            overflowY: 'auto'
          }}
        >
          <SwaggerPanel
            data={graphState}
            searchQuery={searchQuery}
            onEditApi={handleEditApi}
          />
        </div>
      </div>

      {/* In-app Create/Edit Modal */}
      <ApiFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        initialData={editingNode}
        isDark={graphState.isDark}
      />
    </div>
  );
}

export default App;
