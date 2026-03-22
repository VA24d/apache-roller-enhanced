(function() {
    const QA_API = (window.rollerContextPath || '/roller') + '/roller-services/weblog-qa';
    const STORAGE_KEY = 'roller.weblogQa.lastStrategy';
    const RESERVED_PATH_PREFIXES = new Set(['roller-ui', 'roller-services', 'theme', 'webjars']);

    function detectWeblogHandle() {
        const contextPath = (window.rollerContextPath || '/roller').replace(/\/+$/, '');
        let relativePath = window.location.pathname;

        if (contextPath && relativePath.indexOf(contextPath) === 0) {
            relativePath = relativePath.slice(contextPath.length);
        }

        const segments = relativePath.split('/').filter(Boolean);
        if (!segments.length) {
            return null;
        }

        const candidate = segments[0];
        return RESERVED_PATH_PREFIXES.has(candidate) ? null : candidate;
    }

    function injectWidget(weblogHandle) {
        if (!document.body || document.getElementById('roller-weblog-qa-widget')) {
            return;
        }

        const widgetHtml = `
            <section id="roller-weblog-qa-widget" aria-label="Weblog Q and A chatbot">
                <div class="qa-header">
                    <div>
                        <p class="qa-eyebrow">Weblog Q&A Chatbot</p>
                        <h2>Ask this blog</h2>
                    </div>
                    <button class="qa-toggle" type="button" aria-label="Collapse weblog Q and A">−</button>
                </div>
                <div class="qa-body">
                    <label class="qa-label" for="roller-weblog-qa-strategy">Answering strategy</label>
                    <select id="roller-weblog-qa-strategy">
                        <option value="auto">Auto Pick</option>
                        <option value="rag">RAG</option>
                        <option value="long-context">Long Context</option>
                    </select>

                    <label class="qa-label" for="roller-weblog-qa-input">Question about this weblog</label>
                    <textarea id="roller-weblog-qa-input" rows="4" placeholder="What has this blog said about data privacy?"></textarea>

                    <div class="qa-actions">
                        <button id="roller-weblog-qa-ask" type="button">Ask</button>
                        <button id="roller-weblog-qa-fill" type="button" class="qa-secondary">Try Example</button>
                    </div>

                    <p class="qa-status" id="roller-weblog-qa-status">
                        Ask questions grounded in the published posts for <strong>${escapeHtml(weblogHandle)}</strong>.
                    </p>

                    <div class="qa-answer" id="roller-weblog-qa-answer" hidden>
                        <div class="qa-answer-meta" id="roller-weblog-qa-meta"></div>
                        <p class="qa-strategy-note" id="roller-weblog-qa-strategy-note"></p>
                        <p class="qa-answer-text" id="roller-weblog-qa-answer-text"></p>
                        <div class="qa-sources" id="roller-weblog-qa-sources"></div>
                    </div>
                </div>
            </section>
        `;

        document.body.insertAdjacentHTML('beforeend', widgetHtml);

        const widget = document.getElementById('roller-weblog-qa-widget');
        const strategySelect = document.getElementById('roller-weblog-qa-strategy');
        const input = document.getElementById('roller-weblog-qa-input');
        const askButton = document.getElementById('roller-weblog-qa-ask');
        const exampleButton = document.getElementById('roller-weblog-qa-fill');
        const toggleButton = widget.querySelector('.qa-toggle');

        strategySelect.value = loadStoredStrategy();

        toggleButton.addEventListener('click', function() {
            widget.classList.toggle('collapsed');
            toggleButton.textContent = widget.classList.contains('collapsed') ? '+' : '−';
        });

        askButton.addEventListener('click', function() {
            askQuestion(weblogHandle);
        });

        exampleButton.addEventListener('click', function() {
            input.value = 'What has this blog said about data privacy?';
            input.focus();
        });

        input.addEventListener('keydown', function(event) {
            if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
                askQuestion(weblogHandle);
            }
        });

        strategySelect.addEventListener('change', function() {
            storeStrategy(strategySelect.value);
        });
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function loadStoredStrategy() {
        const value = window.localStorage.getItem(STORAGE_KEY);
        if (value === 'auto' || value === 'long-context') {
            return value;
        }
        return value === 'rag' ? 'rag' : 'auto';
    }

    function storeStrategy(value) {
        if (value === 'auto' || value === 'long-context' || value === 'rag') {
            window.localStorage.setItem(STORAGE_KEY, value);
            return;
        }
        window.localStorage.setItem(STORAGE_KEY, 'auto');
    }

    function setStatus(message, isError) {
        const status = document.getElementById('roller-weblog-qa-status');
        if (!status) {
            return;
        }
        status.textContent = message;
        status.classList.toggle('is-error', Boolean(isError));
    }

    function renderAnswer(data) {
        const container = document.getElementById('roller-weblog-qa-answer');
        const meta = document.getElementById('roller-weblog-qa-meta');
        const strategyNote = document.getElementById('roller-weblog-qa-strategy-note');
        const answerText = document.getElementById('roller-weblog-qa-answer-text');
        const sources = document.getElementById('roller-weblog-qa-sources');

        if (!container || !meta || !strategyNote || !answerText || !sources) {
            return;
        }

        const metaParts = [];
        if (data.strategy) {
            metaParts.push('Used ' + formatStrategyLabel(data.strategy));
        }
        if (typeof data.entryCount === 'number') {
            metaParts.push('Scanned ' + data.entryCount + ' published entr' + (data.entryCount === 1 ? 'y' : 'ies'));
        }
        if (typeof data.supportingPassageCount === 'number') {
            metaParts.push('Grounded in ' + data.supportingPassageCount + ' supporting passage' + (data.supportingPassageCount === 1 ? '' : 's'));
        }
        if (data.truncatedContext) {
            metaParts.push('Long-context scan reached its context budget');
        }

        meta.textContent = metaParts.join(' • ');
        strategyNote.textContent = data.strategyReason || '';
        strategyNote.hidden = !data.strategyReason;
        answerText.textContent = data.answer || 'No answer returned.';
        sources.innerHTML = buildSourcesMarkup(Array.isArray(data.sources) ? data.sources : []);
        container.hidden = false;
    }

    function buildSourcesMarkup(items) {
        if (!items.length) {
            return '<p class="qa-no-sources">No supporting sources were returned.</p>';
        }

        return items.map(function(item) {
            const title = escapeHtml(item.title || 'Untitled entry');
            const publishedAt = escapeHtml(item.publishedAt || 'Unknown date');
            const excerpt = escapeHtml(item.excerpt || '');
            const link = item.url
                ? '<a href="' + escapeHtml(item.url) + '" target="_blank" rel="noopener noreferrer">' + title + '</a>'
                : title;
            return `
                <article class="qa-source">
                    <h3>${link}</h3>
                    <p class="qa-source-date">${publishedAt}</p>
                    <p class="qa-source-excerpt">${excerpt}</p>
                </article>
            `;
        }).join('');
    }

    async function askQuestion(weblogHandle) {
        const input = document.getElementById('roller-weblog-qa-input');
        const askButton = document.getElementById('roller-weblog-qa-ask');
        const strategy = document.getElementById('roller-weblog-qa-strategy').value;
        const question = input.value.trim();

        if (!question) {
            setStatus('Enter a question before asking the chatbot.', true);
            input.focus();
            return;
        }

        askButton.disabled = true;
        askButton.textContent = 'Thinking...';
        setStatus('Searching published weblog entries using the ' + formatStrategyLabel(strategy) + ' strategy...', false);

        try {
            storeStrategy(strategy);
            const response = await fetch(QA_API, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    weblogHandle: weblogHandle,
                    question: question,
                    strategy: strategy
                })
            });

            const payload = await response.json();
            if (!response.ok) {
                throw new Error(payload.error || ('Server returned ' + response.status));
            }

            renderAnswer(payload);
            setStatus('Answer ready using ' + formatStrategyLabel(payload.strategy || strategy) + '.', false);
        } catch (error) {
            console.error(error);
            setStatus('Unable to answer right now: ' + error.message, true);
        } finally {
            askButton.disabled = false;
            askButton.textContent = 'Ask';
        }
    }

    function init() {
        const weblogHandle = detectWeblogHandle();
        if (!weblogHandle || document.getElementById('roller-weblog-qa-widget')) {
            return;
        }
        injectWidget(weblogHandle);
    }

    function scheduleInit() {
        init();
        window.setTimeout(init, 150);
        window.setTimeout(init, 800);
    }

    function formatStrategyLabel(strategy) {
        if (strategy === 'long-context') {
            return 'Long Context';
        }
        if (strategy === 'auto') {
            return 'Auto Pick';
        }
        return 'RAG';
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', scheduleInit);
    } else {
        scheduleInit();
    }

    window.addEventListener('load', scheduleInit);
    window.addEventListener('pageshow', scheduleInit);
})();
