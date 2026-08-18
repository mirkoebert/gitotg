(function (global) {
    const MAX_FILE_BYTES = 500 * 1024;
    const MAX_CSV_LINES = 420;

    function countLines(text) {
        if (!text) {
            return 0;
        }
        let lines = 0;
        for (let i = 0; i < text.length; i++) {
            if (text[i] === '\n') {
                lines++;
            }
        }
        if (text.charAt(text.length - 1) !== '\n') {
            lines++;
        }
        return lines;
    }

    function setStatus(statusEl, ok, message) {
        statusEl.replaceChildren();
        const span = document.createElement('span');
        span.className = ok ? 'text-success' : 'text-danger';
        span.textContent = message;
        statusEl.appendChild(span);
        statusEl.style.display = 'block';
    }

    async function validateCsvFile(file, messages) {
        if (!file) {
            return null;
        }
        if (file.size > MAX_FILE_BYTES) {
            return messages.fileTooLarge;
        }
        const text = await file.text();
        if (countLines(text) > MAX_CSV_LINES) {
            return messages.tooManyLines;
        }
        return null;
    }

    function bindImportForm(formId, statusId, messages) {
        const form = document.getElementById(formId);
        const statusEl = document.getElementById(statusId);
        if (!form || !statusEl) {
            return;
        }
        const fileInput = form.querySelector('input[type="file"]');

        async function showLimitErrorIfAny() {
            const file = fileInput && fileInput.files && fileInput.files[0];
            const limitError = await validateCsvFile(file, messages);
            if (limitError) {
                setStatus(statusEl, false, messages.errorPrefix + ' ' + limitError);
                return true;
            }
            statusEl.style.display = 'none';
            statusEl.replaceChildren();
            return false;
        }

        if (fileInput) {
            fileInput.addEventListener('change', function () {
                showLimitErrorIfAny();
            });
        }

        form.addEventListener('submit', async function (e) {
            e.preventDefault();
            const btn = form.querySelector('button[type="submit"]');
            const originalText = btn ? btn.textContent : '';
            if (btn) {
                btn.disabled = true;
                btn.textContent = messages.uploading;
            }
            statusEl.style.display = 'none';
            statusEl.replaceChildren();

            try {
                if (await showLimitErrorIfAny()) {
                    return;
                }
                const formData = new FormData(form);
                const response = await fetch(form.action, {
                    method: 'POST',
                    body: formData
                });
                const text = await response.text();
                if (response.ok) {
                    setStatus(statusEl, true, text);
                    setTimeout(function () {
                        window.location.reload();
                    }, 1200);
                } else {
                    setStatus(statusEl, false, messages.errorPrefix + ' ' + text);
                }
            } catch (err) {
                setStatus(statusEl, false, messages.failedPrefix + ' ' + (err.message || err));
            } finally {
                if (btn) {
                    btn.disabled = false;
                    btn.textContent = originalText;
                }
            }
        });
    }

    global.GitotgCsvImport = {
        bindImportForm: bindImportForm,
        countLines: countLines,
        MAX_FILE_BYTES: MAX_FILE_BYTES,
        MAX_CSV_LINES: MAX_CSV_LINES
    };
})(window);
