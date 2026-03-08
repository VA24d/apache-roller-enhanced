(function() {
    // --- Configuration ---
    const TRANSLATE_API = (window.rollerContextPath || '/roller') + '/roller-services/translate';
    const IGNORE_TAGS = new Set(['SCRIPT', 'STYLE', 'NOSCRIPT', 'CODE', 'PRE', 'IMG', 'IFRAME', 'head', 'link', 'meta']);
    
    // --- State ---
    let originalTextNodes = []; // Array of { node: TextNode, originalText: string }
    
    function injectWidget() {
        const widgetHtml = `
            <div id="roller-translation-widget">
                <div class="widget-header">
                    <span class="title">Web Translation</span>
                    <button class="toggle-btn" onclick="document.getElementById('roller-translation-widget').classList.toggle('collapsed')">−</button>
                </div>
                <div class="widget-body">
                    <div class="form-group">
                        <label for="rt-lang">Target Language:</label>
                        <select id="rt-lang">
                            <option value="hi">Hindi</option>
                            <option value="bn">Bengali</option>
                            <option value="ta">Tamil</option>
                            <option value="te">Telugu</option>
                            <option value="kn">Kannada</option>
                            <option value="en">English</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="rt-provider">Provider:</label>
                        <select id="rt-provider">
                            <option value="mymemory">MyMemory (Free)</option>
                            <option value="sarvam">Sarvam AI</option>
                        </select>
                    </div>
                    <div class="btn-group">
                        <button id="rt-btn-restore" class="action-btn btn-restore">Restore</button>
                        <button id="rt-btn-translate" class="action-btn btn-translate">Translate</button>
                    </div>
                    <div id="rt-status" class="status"></div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', widgetHtml);
        
        document.getElementById('rt-btn-translate').addEventListener('click', handleTranslate);
        document.getElementById('rt-btn-restore').addEventListener('click', restoreOriginalText);
    }
    
    function walkDOM(node, textNodesArr) {
        if (IGNORE_TAGS.has(node.nodeName)) return;
        
        if (node.nodeType === 3) { // Text node
            let text = node.nodeValue.trim();
            if (text.length > 0 && text.match(/[a-zA-Z0-9]/)) { // basic check for actual text
                textNodesArr.push({ node: node, currentText: text });
            }
        } else if (node.nodeType === 1) { // Element node
            // Skip hidden elements or invisible stuff
            const style = window.getComputedStyle(node);
            if (style.display === 'none' || style.visibility === 'hidden') return;
            
            for (let i = 0; i < node.childNodes.length; i++) {
                walkDOM(node.childNodes[i], textNodesArr);
            }
        }
    }
    
    function initOriginalTexts(currentNodesCollection) {
        if (originalTextNodes.length === 0) {
            currentNodesCollection.forEach(item => {
                originalTextNodes.push({
                    node: item.node,
                    originalText: item.node.nodeValue
                });
            });
        }
    }
    
    function restoreOriginalText() {
        if (originalTextNodes.length === 0) return;
        
        originalTextNodes.forEach(item => {
            if (item.node && item.node.parentNode) {
                item.node.nodeValue = item.originalText;
            }
        });
        document.getElementById('rt-status').innerText = 'Original restored.';
    }
    
    async function handleTranslate() {
        const targetLang = document.getElementById('rt-lang').value;
        const provider = document.getElementById('rt-provider').value;
        const btn = document.getElementById('rt-btn-translate');
        const status = document.getElementById('rt-status');
        
        btn.disabled = true;
        btn.innerHTML = '<span class="roller-spinner"></span>Translating...';
        status.innerText = 'Extracting text...';
        
        try {
            // Find container, restrict translation to entry content if possible, else body
            let container = document.querySelector('.content') || document.body;
            
            let nodesCollection = [];
            walkDOM(container, nodesCollection);
            initOriginalTexts(nodesCollection);
            
            if (nodesCollection.length === 0) {
                status.innerText = 'No text found to translate.';
                return;
            }
            
            status.innerText = 'Translating ' + nodesCollection.length + ' chunks...';
            
            // Chunk texts to avoid massive payloads (especially for MyMemory API limits)
            const chunkSize = 20;
            for (let i = 0; i < nodesCollection.length; i += chunkSize) {
                const chunk = nodesCollection.slice(i, i + chunkSize);
                const texts = chunk.map(item => item.currentText);
                
                const response = await fetch(TRANSLATE_API, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        text: texts,
                        sourceLang: 'en',
                        targetLang: targetLang,
                        provider: provider
                    })
                });
                
                if (!response.ok) {
                    throw new Error('Server returned ' + response.status);
                }
                
                const data = await response.json();
                if (data.error) {
                    throw new Error(data.error);
                }
                
                // Replace text in DOM
                if (data.translations && data.translations.length === chunk.length) {
                    for (let j = 0; j < chunk.length; j++) {
                        // Replace the exact text piece (handling surrounding whitespace properly)
                        let translated = data.translations[j];
                        if (translated && translated !== chunk[j].currentText) {
                            chunk[j].node.nodeValue = chunk[j].node.nodeValue.replace(chunk[j].currentText, translated);
                            chunk[j].currentText = translated; // Update current tracked text
                        }
                    }
                }
            }
            
            status.innerText = 'Translation complete.';
        } catch (err) {
            console.error(err);
            status.innerText = 'Error: ' + err.message;
            status.style.color = '#d9534f';
        } finally {
            btn.disabled = false;
            btn.innerText = 'Translate';
        }
    }
    
    // Inject on DOM Content Loaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', injectWidget);
    } else {
        injectWidget();
    }
})();
