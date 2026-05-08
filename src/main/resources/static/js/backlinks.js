/**
 * 反向引用 Offcanvas 通用逻辑（跨技能内容页复用，只读）
 *
 * 使用方式：
 *   在页面底部添加 id="backlinksOffcanvas" 的 Offcanvas，
 *   引入本脚本后调用 BacklinksPanel.open(resource, id, label)
 */
const BacklinksPanel = (() => {
    let _offcanvas = null;

    const SOURCE_TYPE_LABELS = {
        LISTENING: '听力题',
        READING:   '阅读题',
        WRITING:   '写作题',
        SPEAKING:  '口语话题',
    };

    const LINK_TYPE_LABELS = {
        vocabulary:    '词汇',
        paraphrase:    '同义替换考点',
        grammar:       '语法应用',
        pronunciation: '发音规律',
        signal:        '信号词',
    };

    function _getOffcanvas() {
        if (!_offcanvas) _offcanvas = new bootstrap.Offcanvas(document.getElementById('backlinksOffcanvas'));
        return _offcanvas;
    }

    async function open(resource, id, label) {
        document.getElementById('backlinksTitle').textContent = `"${label}" 被引用的位置`;
        _getOffcanvas().show();
        const list = document.getElementById('backlinksList');
        list.innerHTML = '<div class="text-muted small py-2">加载中…</div>';
        try {
            const links = await Api.links.backlinks(resource, id);
            _renderBacklinks(links);
        } catch (e) {
            list.innerHTML = `<div class="text-danger small">${escHtml(e.message)}</div>`;
        }
    }

    function _renderBacklinks(links) {
        const list = document.getElementById('backlinksList');
        if (!links.length) {
            list.innerHTML = '<div class="text-muted small py-2">暂无引用</div>';
            return;
        }
        // 按 sourceType 分组（反向查询中 source_type = 技能内容类型）
        const groups = {};
        links.forEach(l => {
            const g = l.sourceType;
            if (!groups[g]) groups[g] = [];
            groups[g].push(l);
        });
        // 按固定顺序展示
        const order = ['LISTENING', 'READING', 'WRITING', 'SPEAKING'];
        list.innerHTML = order.filter(t => groups[t]).map(type => `
        <div class="mb-3">
          <div class="fw-semibold text-secondary small mb-1">${SOURCE_TYPE_LABELS[type] || type} <span class="badge bg-secondary">${groups[type].length}</span></div>
          ${groups[type].map(l => `
          <div class="d-flex align-items-center border rounded px-2 py-1 mb-1 bg-white gap-2">
            <span class="badge bg-light text-dark border flex-shrink-0">${SOURCE_TYPE_LABELS[l.sourceType] || l.sourceType}</span>
            <span class="fw-semibold small">${escHtml(l.sourceSummary || '')}</span>
            ${l.sourceDetail ? `<span class="text-muted small flex-shrink-0">${escHtml(_formatSourceDetail(l))}</span>` : ''}
            <span class="badge bg-info-subtle text-info-emphasis border border-info-subtle flex-shrink-0">${LINK_TYPE_LABELS[l.linkType] || l.linkType || '-'}</span>
            ${l.note ? `<span class="text-muted small text-truncate" style="max-width:220px">${escHtml(l.note)}</span>` : ''}
          </div>`).join('')}
        </div>`).join('');
    }

    function _formatSourceDetail(link) {
        if (link.sourceType === 'LISTENING') return `Section ${link.sourceDetail}`;
        if (link.sourceType === 'WRITING') return `Task ${link.sourceDetail}`;
        if (link.sourceType === 'SPEAKING') return `Part ${link.sourceDetail}`;
        return link.sourceDetail;
    }

    return { open };
})();
