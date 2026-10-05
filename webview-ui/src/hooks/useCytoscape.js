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

export function useCytoscape(containerRef, graphState, isPanMode) {
    const cyRef = useRef(null);
    const ehRef = useRef(null);

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

        const handleWheel = (e) => {
            if (e.ctrlKey || e.metaKey) return;
            e.preventDefault();
            e.stopImmediatePropagation();
            if (cyRef.current) cyRef.current.panBy({ x: -e.deltaX, y: -e.deltaY });
        };

        try {
            const cy = cytoscape({
                container: currentContainer,
                layout: { name: 'fcose', packComponents: true },
                userPanningEnabled: true,
                userZoomingEnabled: true,
                wheelSensitivity: 0.1,
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
                handleLineColor: THEME_COLORS.supportCacheColor,
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
                    if (targetNode.data('supportsCache')) {
                        KotlinBridge.linkNodes(sourceNode.data('signature'), targetNode.data('signature'));
                    } else {
                        KotlinBridge.showError(`Target API "${targetNode.data('label')}" does not support caching. Please add @SupportCache to it first.`);
                    }
                    // Remove the temporary visual edge drawn by edgehandles
                    addedEles.remove();
                }
            });

            // Hide handle when hovering over the empty canvas background
            // cy.on('mouseover', 'core', () => {
            //     if (ehRef.current) {
            //         ehRef.current.hide();
            //     }
            // });

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
            }
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
        if (ehRef.current) isPanMode ? ehRef.current.disable() : ehRef.current.enable();
    }, [isPanMode]);

    return cyRef;
}
