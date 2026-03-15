(function() {
    const TRANSLATE_API = (window.rollerContextPath || '/roller') + '/roller-services/translate';
    const CONFIG_STORAGE_KEY = 'roller.translation.lastConfig';
    const IGNORE_TAGS = new Set(['SCRIPT', 'STYLE', 'NOSCRIPT', 'CODE', 'PRE', 'IMG', 'IFRAME', 'SVG', 'HEAD', 'LINK', 'META']);
    const PRIMARY_SECTION_SELECTOR = 'h1, h2, h3, h4, h5, h6, p, li, blockquote, figcaption, td, th, .entryTitle, .entryText, .post, .entry, .day';
    const EXCLUDED_SECTION_SELECTOR = '#roller-translation-widget, .rightbar, .comments-form, .next-previous, .rCategory, form, nav';
    const SUPPORTED_LANGUAGES = [
        { value: 'en', label: 'English' },
        { value: 'hi', label: 'Hindi' },
        { value: 'bn', label: 'Bengali' },
        { value: 'ta', label: 'Tamil' },
        { value: 'te', label: 'Telugu' },
        { value: 'kn', label: 'Kannada' },
        { value: 'mr', label: 'Marathi' }
    ];

    const originalTextByNode = new WeakMap();
    let activeTranslation = null;
    let detectedPageSourceLanguage = null;

    function injectWidget() {
        const widgetHtml = `
            <div id="roller-translation-widget">
                <div class="widget-header">
                    <span class="title">Web Translation</span>
                    <button class="toggle-btn" type="button" aria-label="Toggle translation widget">−</button>
                </div>
                <div class="widget-body">
                    <div class="form-group">
                        <label for="rt-source-lang">Source Language:</label>
                        <select id="rt-source-lang">
                            <option value="auto">Auto Detect</option>
                            ${buildLanguageOptions()}
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="rt-lang">Target Language:</label>
                        <select id="rt-lang">
                            ${buildLanguageOptions()}
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
                        <button id="rt-btn-restore" class="action-btn btn-restore" type="button">Restore</button>
                        <button id="rt-btn-translate" class="action-btn btn-translate" type="button">Translate</button>
                    </div>
                    <div id="rt-status" class="status"></div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', widgetHtml);
        const widget = document.getElementById('roller-translation-widget');
        widget.querySelector('.toggle-btn').addEventListener('click', function() {
            widget.classList.toggle('collapsed');
        });

        detectedPageSourceLanguage = detectSourceLanguage();
        applyStoredConfig();
        document.getElementById('rt-btn-translate').addEventListener('click', function() {
            handleTranslate(false);
        });
        document.getElementById('rt-btn-restore').addEventListener('click', restoreOriginalText);
        maybeAutoTranslate();
    }

    function buildLanguageOptions() {
        return SUPPORTED_LANGUAGES.map(function(language) {
            return '<option value="' + language.value + '">' + language.label + '</option>';
        }).join('');
    }

    function getPrimaryContainer() {
        return document.querySelector('.content, main, article, .entry') || document.body;
    }

    function shouldIgnoreTextNode(node) {
        if (!node || !node.parentElement) {
            return true;
        }

        if (IGNORE_TAGS.has(node.parentElement.nodeName)) {
            return true;
        }

        if (node.parentElement.closest(EXCLUDED_SECTION_SELECTOR)) {
            return true;
        }

        return getSourceText(node).trim().length === 0;
    }

    function isVisibleElement(element) {
        const style = window.getComputedStyle(element);
        return style.display !== 'none' && style.visibility !== 'hidden';
    }

    function captureOriginalText(node) {
        if (!originalTextByNode.has(node)) {
            originalTextByNode.set(node, node.nodeValue);
        }
    }

    function getSourceText(node) {
        return originalTextByNode.has(node) ? originalTextByNode.get(node) : node.nodeValue;
    }

    function getSectionRoot(node, container) {
        let element = node.parentElement;
        while (element && element !== container) {
            if (element.matches(PRIMARY_SECTION_SELECTOR)) {
                return element;
            }
            element = element.parentElement;
        }
        return container;
    }

    function buildSectionId(root, container, fallbackIndex) {
        if (root === container) {
            return 'container-' + fallbackIndex;
        }

        const segments = [];
        let element = root;
        while (element && element !== container) {
            let position = 0;
            let sibling = element;
            while (sibling.previousElementSibling) {
                sibling = sibling.previousElementSibling;
                if (sibling.nodeName === element.nodeName) {
                    position++;
                }
            }
            segments.unshift(element.nodeName.toLowerCase() + ':' + position);
            element = element.parentElement;
        }
        return segments.join('/');
    }

    function collectSections() {
        const container = getPrimaryContainer();
        const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT, null);
        const sectionMap = new Map();
        let node;

        while ((node = walker.nextNode())) {
            if (shouldIgnoreTextNode(node)) {
                continue;
            }

            if (!isVisibleElement(node.parentElement)) {
                continue;
            }

            captureOriginalText(node);
            const sectionRoot = getSectionRoot(node, container);
            const sectionId = buildSectionId(sectionRoot, container, sectionMap.size);

            if (!sectionMap.has(sectionId)) {
                sectionMap.set(sectionId, {
                    sectionId: sectionId,
                    items: []
                });
            }

            sectionMap.get(sectionId).items.push({
                node: node,
                sourceText: getSourceText(node).trim()
            });
        }

        return Array.from(sectionMap.values()).filter(function(section) {
            return section.items.length > 0;
        });
    }

    function replaceNodeText(node, sourceText, translatedText) {
        if (!translatedText || translatedText === sourceText) {
            return;
        }

        const currentValue = node.nodeValue;
        const sourceIndex = currentValue.indexOf(sourceText);
        if (sourceIndex === -1) {
            node.nodeValue = translatedText;
            return;
        }

        node.nodeValue = currentValue.slice(0, sourceIndex)
            + translatedText
            + currentValue.slice(sourceIndex + sourceText.length);
    }

    function getStatusElement() {
        return document.getElementById('rt-status');
    }

    function setStatus(message, isError) {
        const status = getStatusElement();
        if (!status) {
            return;
        }

        status.innerText = message;
        status.style.color = isError ? '#d9534f' : '#888';
    }

    function sanitizeStoredConfig(rawConfig) {
        if (!rawConfig || typeof rawConfig !== 'object') {
            return null;
        }

        const validLanguageValues = SUPPORTED_LANGUAGES.map(function(language) {
            return language.value;
        });
        const sourceLang = rawConfig.sourceLang === 'auto' || validLanguageValues.indexOf(rawConfig.sourceLang) !== -1
            ? rawConfig.sourceLang : 'auto';
        const targetLang = validLanguageValues.indexOf(rawConfig.targetLang) !== -1
            ? rawConfig.targetLang : 'hi';
        const provider = rawConfig.provider === 'sarvam' ? 'sarvam' : 'mymemory';

        return {
            sourceLang: sourceLang || 'auto',
            targetLang: targetLang,
            provider: provider,
            autoApply: rawConfig.autoApply !== false
        };
    }

    function storeConfig(config) {
        const persistableConfig = sanitizeStoredConfig({
            sourceLang: config.sourceLang,
            targetLang: config.targetLang,
            provider: config.provider,
            autoApply: config.autoApply
        });
        window.localStorage.setItem(CONFIG_STORAGE_KEY, JSON.stringify(persistableConfig));
    }

    function loadStoredConfig() {
        const rawValue = window.localStorage.getItem(CONFIG_STORAGE_KEY);
        if (!rawValue) {
            return null;
        }

        try {
            return sanitizeStoredConfig(JSON.parse(rawValue));
        } catch (error) {
            window.localStorage.removeItem(CONFIG_STORAGE_KEY);
            return null;
        }
    }

    function applyStoredConfig() {
        const storedConfig = loadStoredConfig();
        document.getElementById('rt-source-lang').value =
            storedConfig && storedConfig.sourceLang ? storedConfig.sourceLang : 'auto';
        document.getElementById('rt-provider').value =
            storedConfig && storedConfig.provider ? storedConfig.provider : 'mymemory';
        document.getElementById('rt-lang').value =
            storedConfig && storedConfig.targetLang ? storedConfig.targetLang : 'hi';
    }

    function detectSourceLanguageFromPath() {
        const pathSegments = window.location.pathname.split('/').filter(Boolean);
        for (let index = 0; index < pathSegments.length; index++) {
            const candidate = pathSegments[index].toLowerCase().split('-')[0].split('_')[0];
            if (SUPPORTED_LANGUAGES.some(function(language) {
                return language.value === candidate;
            })) {
                return candidate;
            }
        }
        return null;
    }

    function detectSourceLanguage() {
        const htmlLang = (document.documentElement.lang || '').trim();
        if (htmlLang) {
            const normalized = htmlLang.toLowerCase().split('-')[0].split('_')[0];
            if (SUPPORTED_LANGUAGES.some(function(language) {
                return language.value === normalized;
            })) {
                return normalized;
            }
        }

        return detectSourceLanguageFromPath() || 'en';
    }

    function buildActiveConfig() {
        const selectedSource = document.getElementById('rt-source-lang').value;
        const resolvedSourceLang = selectedSource === 'auto'
            ? (detectedPageSourceLanguage || detectSourceLanguage())
            : selectedSource;
        return {
            sourceLang: selectedSource,
            resolvedSourceLang: resolvedSourceLang,
            targetLang: document.getElementById('rt-lang').value,
            provider: document.getElementById('rt-provider').value,
            autoApply: true
        };
    }

    function restoreOriginalText() {
        collectSections().forEach(function(section) {
            section.items.forEach(function(item) {
                item.node.nodeValue = getSourceText(item.node);
            });
        });
        activeTranslation = null;
        setStatus('Original restored.', false);
    }

    async function translateSections(config, sections) {
        const response = await fetch(TRANSLATE_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                sourceLang: config.resolvedSourceLang,
                targetLang: config.targetLang,
                provider: config.provider,
                sections: sections.map(function(section) {
                    return {
                        sectionId: section.sectionId,
                        texts: section.items.map(function(item) {
                            return item.sourceText;
                        })
                    };
                })
            })
        });

        if (!response.ok) {
            throw new Error('Server returned ' + response.status);
        }

        const data = await response.json();
        if (data.error) {
            throw new Error(data.error);
        }

        return data;
    }

    async function handleTranslate(isAutoApply) {
        const config = buildActiveConfig();
        const translateButton = document.getElementById('rt-btn-translate');
        const sections = collectSections();

        if (sections.length === 0) {
            setStatus('No primary weblog text found to translate.', false);
            return;
        }

        if (config.resolvedSourceLang === config.targetLang) {
            setStatus('Source and target languages are the same.', false);
            return;
        }

        translateButton.disabled = true;
        translateButton.innerHTML = '<span class="roller-spinner"></span>Translating...';
        setStatus(
            (isAutoApply ? 'Applying saved translation...' : 'Checking cached sections...')
            + (config.sourceLang === 'auto' ? ' Auto-detected source: ' + config.resolvedSourceLang + '.' : ''),
            false
        );

        try {
            const data = await translateSections(config, sections);
            const responseSections = Array.isArray(data.sections) ? data.sections : [];

            responseSections.forEach(function(responseSection, index) {
                const currentSection = sections[index];
                if (!currentSection || !Array.isArray(responseSection.translations)) {
                    return;
                }

                currentSection.items.forEach(function(item, itemIndex) {
                    replaceNodeText(item.node, item.sourceText, responseSection.translations[itemIndex]);
                });
            });

            const meta = data.meta || {};
            const cachedSections = typeof meta.cachedSections === 'number' ? meta.cachedSections : 0;
            const translatedSections = typeof meta.translatedSections === 'number'
                ? meta.translatedSections : responseSections.length;
            activeTranslation = config;
            storeConfig(config);
            setStatus(
                'Translation complete. Reused ' + cachedSections + ' cached section(s), translated '
                + translatedSections + ' section(s).',
                false
            );
        } catch (error) {
            console.error(error);
            setStatus('Error: ' + error.message, true);
        } finally {
            translateButton.disabled = false;
            translateButton.innerText = 'Translate';
        }
    }

    function maybeAutoTranslate() {
        const storedConfig = loadStoredConfig();
        if (!storedConfig || !storedConfig.autoApply) {
            return;
        }

        document.getElementById('rt-source-lang').value = storedConfig.sourceLang || 'auto';
        document.getElementById('rt-lang').value = storedConfig.targetLang || 'hi';
        document.getElementById('rt-provider').value = storedConfig.provider || 'mymemory';
        handleTranslate(true);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', injectWidget);
    } else {
        injectWidget();
    }
})();
