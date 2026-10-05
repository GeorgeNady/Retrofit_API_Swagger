export const KotlinBridge = {
    send: (type, payload = {}) => {
        if (window.cefQuery) {
            window.cefQuery({
                request: JSON.stringify({ type, ...payload }),
                onSuccess: () => {},
                onFailure: (err) => console.error("Kotlin bridge error:", err)
            });
        } else {
            console.warn("cefQuery not found. Check if running inside JCEF.");
        }
    },
    log: (message) => KotlinBridge.send('log', { message }),
    notifyReady: () => {
        const trySend = () => window.cefQuery ? KotlinBridge.send('ready') : setTimeout(trySend, 100);
        trySend();
    },
    linkNodes: (source, target) => KotlinBridge.send('linkNodes', { source, target }),
    selectNode: (signature) => KotlinBridge.send('nodeSelected', { signature }),
    showError: (message) => KotlinBridge.send('error', { message }),
    navigateToSource: (signature) => KotlinBridge.send('navigateToSource', { signature }),
    executeApiCall: (signature, url, body) => KotlinBridge.send('executeApiCall', { signature, url, body }),
    copyToClipboard: (text) => KotlinBridge.send('copyToClipboard', { text }),
    navigateToType: (typeName, interfaceClassName) => KotlinBridge.send('navigateToType', { typeName, interfaceClassName }),
    requestSchema: (fqn) => KotlinBridge.send('requestSchema', { fqn }),
    createOrUpdateApi: (payload) => KotlinBridge.send('createOrUpdateApi', payload),
    executeEdgeAction: (actionId, sourceSignature, targetSignature) =>
        KotlinBridge.send('executeEdgeAction', { actionId, source: sourceSignature, target: targetSignature })
};
