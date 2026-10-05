(() => {
    document.querySelectorAll('[data-favorite-form]').forEach(form => form.addEventListener('submit', () => {
        const button = form.querySelector('button'); if (button) button.disabled = true;
    }));
    const panel = document.querySelector('[data-share-panel]');
    if (panel) {
        const toggle = document.querySelector('[data-share-toggle]');
        const input = panel.querySelector('[data-share-url]');
        const feedback = panel.querySelector('[data-share-feedback]');
        input.value = window.location.origin + window.location.pathname + '?id=' + encodeURIComponent(panel.dataset.videoId);
        toggle.addEventListener('click', () => { panel.hidden = !panel.hidden; toggle.setAttribute('aria-expanded', String(!panel.hidden)); if (!panel.hidden) { input.focus(); input.select(); } });
        panel.querySelector('[data-share-copy]').addEventListener('click', async event => {
            const button = event.currentTarget; button.disabled = true;
            try {
                if (!navigator.clipboard) throw new Error('clipboard-unavailable');
                await navigator.clipboard.writeText(input.value);
                feedback.textContent = 'Đã sao chép đường dẫn.';
                if (panel.dataset.signedIn === 'true') {
                    const result = await fetch(panel.dataset.shareEndpoint, { method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: new URLSearchParams({ id: panel.dataset.videoId, csrfToken: panel.dataset.csrf }) });
                    const data = await result.json();
                    if (result.ok && Number.isInteger(data.shareCount)) document.querySelector('[data-share-count]').textContent = String(data.shareCount);
                    else feedback.textContent = 'Đã sao chép đường dẫn. ' + (data.error || 'Chưa ghi nhận được lượt chia sẻ.');
                }
            } catch (error) {
                if (feedback.textContent.startsWith('Đã sao chép')) feedback.textContent = 'Đã sao chép đường dẫn. Chưa ghi nhận được lượt chia sẻ.';
                else { input.focus(); input.select(); feedback.textContent = 'Trình duyệt chưa cho phép sao chép tự động. Bạn có thể sao chép đường dẫn đã chọn.'; }
            } finally { button.disabled = false; }
        });
    }
    document.querySelectorAll('[data-copy-code]').forEach(button => button.addEventListener('click', async () => {
        const code = document.getElementById(button.dataset.copyCode);
        const status = button.parentElement.querySelector('[data-code-feedback]');
        try { await navigator.clipboard.writeText(code.value); status.textContent = 'Đã sao chép'; }
        catch (error) { code.focus(); code.select(); status.textContent = 'Sao chép mã đã chọn'; }
    }));
})();
