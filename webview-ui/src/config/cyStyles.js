// Define standard colors to maintain consistency across the graph
export const THEME_COLORS = {
    // Semantic Colors
    invalidateCacheColor: '#FF5252',
    supportCacheColor: '#ffc800',

    // Card & Node Elements (Matches ApiNode.css)
    nodeBg: '#3c3f41',
    nodeBorder: '#4e5157',
    classBg: 'rgba(43, 45, 48, 0.3)',

    // Base Colors
    white: '#ffffff',
    darkBg: '#1e1f22',

    // Theming (Text & standard edges)
    textLight: '#1e1f22',
    textDark: '#dfe1e5',
    edgeLight: '#cccccc',
    edgeDark: '#555555'
};

export const getCyStyle = (isDark) => {
    const textColor = isDark ? THEME_COLORS.textDark : THEME_COLORS.textLight;
    const edgeColor = isDark ? THEME_COLORS.edgeDark : THEME_COLORS.edgeLight;
    const textBgColor = isDark ? THEME_COLORS.darkBg : THEME_COLORS.white;

    return [
        {
            selector: 'node',
            style: {
                'font-family': 'sans-serif',
                'font-size': '11px',
                'color': textColor,
            }
        },
        {
            selector: 'node[type="class"]',
            style: {
                'background-color': THEME_COLORS.classBg,
                'shape': 'round-rectangle',
                'border-width': 1,
                'border-color': edgeColor,
                'border-style': 'dashed',
                'label': 'data(label)',
                'text-valign': 'top',
                'text-margin-y': '-15px',
                'padding': '40px'
            }
        },
        {
            selector: 'node[type="method"]',
            style: {
                'shape': 'round-rectangle',
                'background-color': 'transparent',
                'border-width': 0,
                'width': 266,
                'height': 75,
                'overlay-opacity': 0,
                'background-opacity': 0,
            }
        },
        {
            selector: 'edge',
            style: {
                'width': 2,
                'line-color': edgeColor,
                'target-arrow-color': edgeColor,
                'target-arrow-shape': 'triangle',
                'curve-style': 'bezier',
                'arrow-scale': 1.2
            }
        },
        {
            selector: 'edge[type="invalidation"]',
            style: {
                // --- Gradient Properties ---
                'line-fill': 'linear-gradient',
                'line-gradient-stop-colors': `${THEME_COLORS.invalidateCacheColor} ${THEME_COLORS.supportCacheColor}`,
                'line-gradient-stop-positions': '0 100',

                // Match the arrow to the ending color of the gradient
                'target-arrow-color': THEME_COLORS.supportCacheColor,

                // --- Original Properties ---
                'line-style': 'dashed',
                'label': 'Invalidates',
                'font-size': '9px',
                'color': THEME_COLORS.invalidateCacheColor,
                'text-background-color': textBgColor,
                'text-background-opacity': 1,
                'text-background-padding': '2px'
            }
        },
        {
            selector: '.eh-handle',
            style: {
                'background-color': THEME_COLORS.nodeBg,
                'width': 12,
                'height': 12,
                'shape': 'ellipse',
                'overlay-opacity': 0,
                'border-width': 2,
                'border-color': THEME_COLORS.nodeBorder,
                'z-index': 9999,
                'transition-property': 'background-color, border-color',
                'transition-duration': 200
            }
        },
        {
            selector: '.eh-handle:hover',
            style: {
                'background-color': THEME_COLORS.invalidateCacheColor,
                'border-color': THEME_COLORS.nodeBorder
            }
        },
        {
            selector: '.eh-preview, .eh-ghost-edge',
            style: {
                'background-color': THEME_COLORS.supportCacheColor,
                'line-color': THEME_COLORS.supportCacheColor,
                'target-arrow-color': THEME_COLORS.supportCacheColor,
                'target-arrow-shape': 'triangle',
                'line-style': 'dashed'
            }
        },
        {
            selector: '.dimmed',
            style: {
                'opacity': 0.22,
                'transition-property': 'opacity',
                'transition-duration': '0.15s'
            }
        },
        {
            selector: '.highlighted',
            style: {
                'opacity': 1.0,
                'transition-property': 'opacity',
                'transition-duration': '0.15s'
            }
        }
    ];
};