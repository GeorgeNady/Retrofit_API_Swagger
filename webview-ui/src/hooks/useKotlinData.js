import { useState, useEffect } from 'react';
import { KotlinBridge } from '../api/KotlinBridge';

export function useKotlinData() {
    const editorQuery = new URLSearchParams(window.location.search).get('editor');
    const initialIsEditor = editorQuery === 'true';

    const [graphState, setGraphState] = useState({
        endpoints: [],
        colors: {},
        isDark: true,
        requestResults: {},
        schemas: {},
        isEditorMode: initialIsEditor
    });

    useEffect(() => {
        window.updateGraphData = (jsonData, isDark) => {
            try {
                const parsed = typeof jsonData === 'string' ? JSON.parse(jsonData) : jsonData;
                const finalData = typeof parsed === 'string' ? JSON.parse(parsed) : parsed;

                setGraphState(prev => ({
                    endpoints: finalData.endpoints || [],
                    colors: finalData.colors || {},
                    isDark: isDark !== undefined ? isDark : (finalData.isDark !== undefined ? finalData.isDark : true),
                    requestResults: finalData.requestResults ? { ...prev.requestResults, ...finalData.requestResults } : prev.requestResults,
                    schemas: finalData.schemas ? { ...prev.schemas, ...finalData.schemas } : prev.schemas,
                    isEditorMode: finalData.isEditorMode !== undefined ? finalData.isEditorMode : prev.isEditorMode
                }));

                console.log("Retrofit Swagger Debug: Data updated from Kotlin", finalData.endpoints?.length, "endpoints");
            } catch (e) {
                console.error("Update Parse Error:", e);
                KotlinBridge.log("Parse Error: " + e.message);
            }
        };

        window.updateResponseResult = (signature, responseText) => {
            setGraphState(prev => ({
                ...prev,
                requestResults: {
                    ...prev.requestResults,
                    [signature]: responseText
                }
            }));
        };

        window.updateSchema = (fqn, schemaJson) => {
            setGraphState(prev => ({
                ...prev,
                schemas: {
                    ...prev.schemas,
                    [fqn]: schemaJson
                }
            }));
        };

        KotlinBridge.notifyReady();
    }, []);

    return graphState;
}
