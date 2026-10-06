/**
 * Utility functions for response formatting, format detection,
 * syntax highlighting, and display helpers.
 */

/**
 * Escapes special HTML characters to prevent XSS / render issues.
 */
export function escapeHtml(str) {
    if (!str || typeof str !== 'string') return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

/**
 * Automatically detects response content format.
 * Returns: 'json' | 'xml' | 'html' | 'yaml' | 'text'
 */
export function detectFormat(str) {
    if (!str || typeof str !== 'string') return 'text';
    const trimmed = str.trim();
    if (trimmed.startsWith('Error:')) return 'text';

    // JSON detection
    if ((trimmed.startsWith('{') && trimmed.endsWith('}')) || (trimmed.startsWith('[') && trimmed.endsWith(']'))) {
        try {
            JSON.parse(trimmed);
            return 'json';
        } catch (e) {}
    }

    // HTML detection
    if (/^<!DOCTYPE\s+html/i.test(trimmed) || /<html[\s>]/i.test(trimmed) || /<body[\s>]/i.test(trimmed)) {
        return 'html';
    }

    // XML detection
    if (/^<\?xml/i.test(trimmed) || (/^<[a-zA-Z0-9:_-]+(\s+[^>]*)?>[\s\S]*<\/[a-zA-Z0-9:_-]+>$/i.test(trimmed) && !trimmed.includes('<html'))) {
        return 'xml';
    }

    // YAML detection
    if (/^(---\s*\n)?([a-zA-Z0-9_-]+:\s+[^\n]*\n?){2,}/m.test(trimmed)) {
        return 'yaml';
    }

    return 'text';
}

/**
 * Formats JSON or NDJSON with 2-space indentation.
 */
export function formatJson(text) {
    if (!text || typeof text !== 'string') return '';
    try {
        const obj = JSON.parse(text);
        return JSON.stringify(obj, null, 2);
    } catch {
        // Maybe NDJSON (newline-delimited JSON)?
        const lines = text.trim().split('\n');
        if (lines.length > 1) {
            try {
                const parsed = lines.map(l => JSON.parse(l.trim()));
                return parsed.map(item => JSON.stringify(item, null, 2)).join('\n\n');
            } catch {
                return text;
            }
        }
        return text;
    }
}

/**
 * Formats XML / HTML with clean 2-space tag indentation.
 */
export function formatXml(xml) {
    if (!xml || typeof xml !== 'string') return '';
    try {
        let formatted = '';
        const reg = /(>)(<)(\/*)/g;
        let w = xml.replace(reg, '$1\r\n$2$3');
        let pad = 0;
        const lines = w.split('\r\n');
        for (let i = 0; i < lines.length; i++) {
            let node = lines[i].trim();
            if (!node) continue;
            let indent = 0;
            if (node.match(/.+<\/\w[^>]*>$/)) {
                indent = 0;
            } else if (node.match(/^<\/\w/)) {
                if (pad !== 0) pad -= 1;
            } else if (node.match(/^<\w[^>]*[^\/]>.*$/) && !node.startsWith('<?') && !node.startsWith('<!')) {
                indent = 1;
            } else {
                indent = 0;
            }

            let padding = '  '.repeat(pad);
            formatted += padding + node + '\n';
            pad += indent;
        }
        return formatted.trim();
    } catch {
        return xml;
    }
}

/**
 * Syntax highlights JSON string into HTML spans.
 */
export function highlightJson(jsonStr) {
    if (!jsonStr) return '';
    return jsonStr.replace(
        /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?|[{}[\],])/g,
        (match) => {
            const escaped = escapeHtml(match);
            let cls = 'swagger-token-number';
            if (match.startsWith('"')) {
                if (match.endsWith(':')) {
                    cls = 'swagger-token-key';
                } else {
                    cls = 'swagger-token-string';
                }
            } else if (match === 'true' || match === 'false') {
                cls = 'swagger-token-boolean';
            } else if (match === 'null') {
                cls = 'swagger-token-null';
            } else if (/^[{}[\],]$/.test(match)) {
                cls = 'swagger-token-punct';
            }
            return `<span class="${cls}">${escaped}</span>`;
        }
    );
}

/**
 * Syntax highlights XML / HTML string into HTML spans.
 */
export function highlightXml(xmlStr) {
    if (!xmlStr) return '';
    const parts = xmlStr.split(/(<[^>]+>)/g);
    return parts.map(part => {
        if (!part) return '';
        if (part.startsWith('<!--')) {
            return `<span class="swagger-token-comment">${escapeHtml(part)}</span>`;
        }
        if (part.startsWith('<?') || part.startsWith('<!DOCTYPE')) {
            return `<span class="swagger-token-doctype">${escapeHtml(part)}</span>`;
        }
        if (part.startsWith('<')) {
            return part.replace(
                /^(<\/?)([a-zA-Z0-9:_-]+)?((?:\s+[a-zA-Z0-9:_-]+(?:=(?:"[^"]*"|'[^']*'|[^\s>]+))?)*)\s*(\/?>)$/,
                (m, open, tagName, attrs, close) => {
                    let out = `<span class="swagger-token-tag">${escapeHtml(open || '')}${escapeHtml(tagName || '')}</span>`;
                    if (attrs) {
                        out += attrs.replace(
                            /(\s+)([a-zA-Z0-9:_-]+)(?:(=)("[^"]*"|'[^']*'|[^\s>]+))?/g,
                            (m2, sp, name, eq, val) => {
                                let a = `${sp}<span class="swagger-token-attr">${escapeHtml(name)}</span>`;
                                if (eq && val) {
                                    a += `${eq}<span class="swagger-token-string">${escapeHtml(val)}</span>`;
                                }
                                return a;
                            }
                        );
                    }
                    if (close) {
                        out += `<span class="swagger-token-tag">${escapeHtml(close)}</span>`;
                    }
                    return out;
                }
            );
        }
        return escapeHtml(part);
    }).join('');
}

/**
 * Syntax highlights YAML string into HTML spans.
 */
export function highlightYaml(yamlStr) {
    if (!yamlStr) return '';
    const lines = yamlStr.split('\n');
    return lines.map(line => {
        let escaped = escapeHtml(line);
        if (/^\s*#/.test(escaped)) {
            return `<span class="swagger-token-comment">${escaped}</span>`;
        }
        escaped = escaped.replace(/^(\s*[\w.-]+)(:)(\s*)/, (m, k, c, s) => {
            return `<span class="swagger-token-key">${k}</span><span class="swagger-token-punct">${c}</span>${s}`;
        });
        escaped = escaped.replace(/\b(true|false|null|yes|no)\b/gi, '<span class="swagger-token-boolean">$&</span>');
        escaped = escaped.replace(/(:\s+)(-?\d+(?:\.\d+)?)\b/g, '$1<span class="swagger-token-number">$2</span>');
        escaped = escaped.replace(/(".*?"|'.*?')/g, '<span class="swagger-token-string">$1</span>');
        return escaped;
    }).join('\n');
}

/**
 * Formats number of bytes into a human-readable size string (B, KB, MB).
 */
export function formatByteSize(bytes) {
    if (!bytes && bytes !== 0) return '';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}
