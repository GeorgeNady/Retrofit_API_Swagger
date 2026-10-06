// Helper to clean key names (remove PreferenceKey. prefix if present)
const cleanKey = (key) => {
    if (!key) return "";
    const s = String(key);
    return s.includes(".") ? s.split('.').pop() : s;
};

// Helper to check if a node supports caching
const isSupportCache = (node) => {
    return node.annotations.some(a => a.name === 'SupportCache' || a.name.endsWith('.SupportCache'));
};

// Helper to check if a node invalidates cache
const isInvalidatorNode = (node) => {
    return node.invalidatesKeys && node.invalidatesKeys.length > 0;
};

// Extracted function dedicated to resolving invalidation relationships
const extractInvalidationEdges = (endpoints) => {
    const edges = [];

    endpoints.forEach(sourceNode => {
        if (isInvalidatorNode(sourceNode)) {
            sourceNode.invalidatesKeys.forEach(rawKey => {
                const key = cleanKey(rawKey);
                endpoints.forEach(targetNode => {
                    if (targetNode.signature !== sourceNode.signature) {
                        const targetHasKey = targetNode.annotations.some(a => {
                            const isCacheAnno = a.name === 'SupportCache' || a.name.endsWith('.SupportCache');
                            if (!isCacheAnno) return false;

                            // Check all arguments for a matching key
                            return Object.values(a.arguments).some(v => cleanKey(v) === key);
                        });

                        if (targetHasKey) {
                            edges.push({
                                data: {
                                    id: `edge-${sourceNode.signature}-${targetNode.signature}-${key}`,
                                    source: sourceNode.signature,
                                    target: targetNode.signature,
                                    type: 'invalidation'
                                }
                            });
                        }
                    }
                });
            });
        }
    });

    return edges;
};

export const mapEndpointsToElements = (endpoints, colors) => {
    const elements = [];
    const classes = new Set();

    // 1. Extract Nodes (Classes and Methods)
    endpoints.forEach(node => {
        // Extract class nodes
        if (!classes.has(node.className)) {
            elements.push({ data: { id: node.className, label: node.className, type: 'class' } });
            classes.add(node.className);
        }

        const methodType = node.httpMethod.toUpperCase();

        // Extract method nodes
        elements.push({
            data: {
                id: node.signature,
                label: node.methodName,
                path: node.path,
                method: methodType,
                parent: node.className,
                type: 'method',
                signature: node.signature,
                supportsCache: isSupportCache(node),
                isInvalidator: isInvalidatorNode(node),
                methodColors: colors[methodType]
            }
        });
    });

    // 2. Extract Edges
    const edges = extractInvalidationEdges(endpoints);

    // 3. Combine and return
    return [...elements, ...edges];
};
