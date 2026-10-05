import React, { useEffect, useRef } from 'react';
import ReactDOMServer from 'react-dom/server';
import cytoscape from 'cytoscape';
import dagre from 'cytoscape-dagre';
import fcose from 'cytoscape-fcose';
import edgehandles from 'cytoscape-edgehandles';
import nodeHtmlLabel from 'cytoscape-node-html-label';
import { KotlinBridge } from '../api/KotlinBridge';
import { mapEndpointsToElements } from '../utils/mapEndpointsToElements';
import { getCyStyle, THEME_COLORS } from '../config/cyStyles';
import ApiNode from '../components/ApiNode/ApiNode';

// Register plugins
cytoscape.use(dagre);
cytoscape.use(fcose);
cytoscape.use(edgehandles);
nodeHtmlLabel(cytoscape);

export function useCytoscape(containerRef, graphState, isPanMode, onEdgeConnected) {
    const cyRef = useRef(null);
    const ehRef = useRef(null);
    const onEdgeConnectedRef = useRef(onEdgeConnected);

    useEffect(() => {
        onEdgeConnectedRef.current = onEdgeConnected;
    }, [onEdgeConnected]);

    // Initialize Cytoscape
    useEffect(() => {
        if (!containerRef.current) return;

        // Best practice: Store the current ref value for the cleanup function
        const currentContainer = containerRef.current;

        // Define handlers outside the try block so the cleanup function can access them
        const handleMouseLeave = () => {
            if (ehRef.current) {
                ehRef.current.hide();
            }
        };

        // --- Figma / Android NavGraph Standard Touchpad & Gesture Interaction Engine ---
        const handleWheel = (e) => {
            // Prevent default page scroll, bounce, and browser pinch-zoom
            e.preventDefault();
            e.stopImmediatePropagation();

            const cy = cyRef.current;
            if (!cy) return;

            // Normalize delta values across modes (0: pixels, 1: lines, 2: pages)
            let dx = e.deltaX;
            let dy = e.deltaY;
            if (e.deltaMode === 1) { // DOM_DELTA_LINE
                dx *= 20;
                dy *= 20;
            } else if (e.deltaMode === 2) { // DOM_DELTA_PAGE
                dx *= 300;
                dy *= 300;
            }

            const isPinchOrCtrl = e.ctrlKey || e.metaKey;

            if (isPinchOrCtrl) {
                // PINCH-TO-ZOOM: 2-finger touchpad pinch gesture OR Ctrl/Cmd + wheel
                // Smooth exponential zoom curve centered around the exact cursor position
                const clampedDy = Math.max(Math.min(dy, 100), -100);
                const zoomFactor = Math.pow(1.006, -clampedDy);

                const currentZoom = cy.zoom();
                const minZoom = 0.08;
                const maxZoom = 4.0;
                const targetZoom = Math.min(Math.max(currentZoom * zoomFactor, minZoom), maxZoom);

                if (Math.abs(targetZoom - currentZoom) > 0.0001) {
                    const rect = currentContainer.getBoundingClientRect();
                    const renderedPosition = {
                        x: e.clientX - rect.left,
                        y: e.clientY - rect.top
                    };
                    cy.zoom({
                        level: targetZoom,
                        renderedPosition: renderedPosition
                    });
                }
            } else if (e.shiftKey) {
                // Shift + Wheel = Horizontal pan (Figma / Photoshop standard)
                cy.panBy({ x: -dy, y: 0 });
            } else {
                // TWO-FINGER TOUCHPAD SWIPE = 2D Pan (Figma / Android NavGraph standard)
                cy.panBy({ x: -dx, y: -dy });
            }
        };

        // --- Spacebar Pan and Middle-Click Drag (Figma / Photoshop standard) ---
        let isSpacePressed = false;
        let isDragPanning = false;
        let dragStartX = 0;
        let dragStartY = 0;

        const isInputFocused = () => {
            const active = document.activeElement;
            return active && (active.tagName === 'INPUT' || active.tagName === 'TEXTAREA' || active.isContentEditable);
        };

        const handleKeyDown = (e) => {
            if (e.code === 'Space' && !isInputFocused() && !isSpacePressed) {
                isSpacePressed = true;
                currentContainer.style.cursor = 'grab';
                if (ehRef.current) ehRef.current.disable();
            }
        };

        const handleKeyUp = (e) => {
            if (e.code === 'Space') {
                isSpacePressed = false;
                if (isDragPanning) {
                    isDragPanning = false;
                }
                currentContainer.style.cursor = '';
                if (ehRef.current) ehRef.current.enable();
            }
        };

        const handleMouseDown = (e) => {
            // Middle mouse click (button 1) OR Left click with Spacebar held
            if (e.button === 1 || (e.button === 0 && isSpacePressed)) {
                e.preventDefault();
                e.stopImmediatePropagation();
                isDragPanning = true;
                dragStartX = e.clientX;
                dragStartY = e.clientY;
                currentContainer.style.cursor = 'grabbing';
            }
        };

        const handleMouseMove = (e) => {
            if (isDragPanning && cyRef.current) {
                e.preventDefault();
                const dx = e.clientX - dragStartX;
                const dy = e.clientY - dragStartY;
                dragStartX = e.clientX;
                dragStartY = e.clientY;
                cyRef.current.panBy({ x: dx, y: dy });
            }
        };

        const handleMouseUp = (e) => {
            if (isDragPanning) {
                isDragPanning = false;
                currentContainer.style.cursor = isSpacePressed ? 'grab' : '';
            }
        };

        try {
            const cy = cytoscape({
                container: currentContainer,
                layout: { name: 'fcose', packComponents: true },
                userPanningEnabled: true,
                userZoomingEnabled: false, // Handled exclusively by custom Figma/NavGraph touchpad engine
                minZoom: 0.08,
                maxZoom: 4.0,
                boxSelectionEnabled: true,
                selectionType: 'single'
            });
            cyRef.current = cy;

            // HTML Labels - Using React Components
            cy.nodeHtmlLabel([{
                query: 'node[type="method"]',
                tpl: (data) => ReactDOMServer.renderToStaticMarkup(
                    React.createElement(ApiNode, {data: data})
                )
            }]);

            // EdgeHandles
            ehRef.current = cy.edgehandles({
                canConnect: (sourceNode, targetNode) => sourceNode.data('type') === 'method' && targetNode.data('type') === 'method',
                edgeParams: () => ({ data: { type: 'invalidation' } }),
                handleNodes: 'node[type="method"]',

                // UI Styling
                handleSize: 12,
                handlePosition: 'right middle',
                handleColor: THEME_COLORS.white,
                handleLineColor: '#58a6ff',
                handleLineWidth: 2,

                // --- UX Enhancements ---

                // 1. Snapping: Magnetically pulls the arrow to a valid target node when close
                snap: true,
                snapThreshold: 50,

                // 2. Prevent UI glitches: Stops other edges from intercepting the mouse while drawing
                noEdgeEventsInDraw: true,

                // 3. Prevent page scrolling/panning while the user is actively dragging an edge
                disableBrowserGestures: true,

                // 4. Hover delay: How long (ms) to hover before the handle appears (default is 150)
                hoverDelay: 50,

                complete: (sourceNode, targetNode, addedEles) => {
                    // Remove the temporary visual edge drawn by edgehandles
                    addedEles.remove();

                    if (onEdgeConnectedRef.current) {
                        onEdgeConnectedRef.current({
                            source: sourceNode.data(),
                            target: targetNode.data()
                        });
                    } else {
                        KotlinBridge.linkNodes(sourceNode.data('signature'), targetNode.data('signature'));
                    }
                }
            });

            // Event: Selection
            cy.on('tap', 'node', (evt) => {
                const node = evt.target;
                if (node.data('type') === 'method') {
                    KotlinBridge.selectNode(node.data('signature'));
                }
            });

            // Attach native DOM events
            currentContainer.addEventListener('mouseleave', handleMouseLeave);
            currentContainer.addEventListener('wheel', handleWheel, { passive: false, capture: true });
            currentContainer.addEventListener('mousedown', handleMouseDown, { capture: true });
            window.addEventListener('keydown', handleKeyDown);
            window.addEventListener('keyup', handleKeyUp);
            window.addEventListener('mousemove', handleMouseMove);
            window.addEventListener('mouseup', handleMouseUp);

        } catch (err) {
            console.error("Cytoscape Init Error:", err);
            KotlinBridge.log("Init Error: " + err.message);
        }

        // --- Cleanup function ---
        return () => {
            // Remove DOM event listeners
            if (currentContainer) {
                currentContainer.removeEventListener('mouseleave', handleMouseLeave);
                currentContainer.removeEventListener('wheel', handleWheel, { capture: true });
                currentContainer.removeEventListener('mousedown', handleMouseDown, { capture: true });
            }
            window.removeEventListener('keydown', handleKeyDown);
            window.removeEventListener('keyup', handleKeyUp);
            window.removeEventListener('mousemove', handleMouseMove);
            window.removeEventListener('mouseup', handleMouseUp);

            // Destroy cytoscape instance
            if (cyRef.current) {
                cyRef.current.destroy();
                cyRef.current = null;
            }
        };
    }, []);

    // Update Graph Data
    useEffect(() => {
        if (!cyRef.current || !graphState.endpoints.length) return;

        const cy = cyRef.current;
        const newElements = mapEndpointsToElements(graphState.endpoints, graphState.colors);

        // 1. Safely update the stylesheet without overwriting the core instance
        cy.style().fromJson(getCyStyle(graphState.isDark)).update();

        // 2. Check if this is the first load
        const isFirstLoad = cy.elements().length === 0;

        // 3. DIFFING: Safely merge data instead of nuking the graph
        cy.batch(() => {
            const newElesMap = new Map();
            newElements.forEach(e => newElesMap.set(e.data.id, e));

            // Remove elements that no longer exist in the new state
            cy.elements().forEach(existingEle => {
                if (!newElesMap.has(existingEle.id())) {
                    cy.remove(existingEle);
                }
            });

            // Add new elements or update existing ones
            newElements.forEach(newEle => {
                const existingEle = cy.getElementById(newEle.data.id);
                if (existingEle.length === 0) {
                    cy.add(newEle); // It's a new edge, add it
                } else {
                    existingEle.data(newEle.data); // Existing node, just update data quietly
                }
            });
        });

        // 4. Run the layout smoothly using fcose
        const layout = cy.layout({
            name: 'fcose',
            quality: 'default',
            randomize: true,
            animate: true,
            animationDuration: 300,
            fit: true,
            padding: 50,
            packComponents: true,
            nodeDimensionsIncludeLabels: true,
            uniformNodeDimensions: false
        });

        // ONLY zoom out and fit the screen if it is the very first time the graph loads.
        if (isFirstLoad) {
            layout.one('layoutstop', () => cy.fit(null, 50));
        }

        layout.run();
    }, [graphState]);

    // Handle Pan Mode
    useEffect(() => {
        if (!cyRef.current) return;
        const cy = cyRef.current;
        cy.autoungrabify(isPanMode);
        cy.boxSelectionEnabled(!isPanMode);
        if (ehRef.current) isPanMode ? ehRef.current.disable() : ehRef.current.enable();
        if (containerRef.current) {
            containerRef.current.style.cursor = isPanMode ? 'grab' : '';
        }
    }, [isPanMode]);

    return cyRef;
}
