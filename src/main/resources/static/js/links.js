/**
 * 关联管理 Offcanvas 通用逻辑（技能内容页复用）
 *
 * 使用方式：
 *   在页面底部添加 id="linksOffcanvas" 的 Offcanvas，
 *   引入本脚本后调用 LinksPanel.open(resource, id, title)
 */
const LinksPanel = (() => {
    let _resource = null, _id = null;
    let _offcanvas = null;

    // link_type 分组展示标签
    const LINK_TYPE_LABELS = {
        vocabulary:    '词汇',
        paraphrase:    '同义替换考点',
        grammar:       '语法应用',
        pronunciation: '发音规律',
        signal:        '信号词',
    };

    // targetType 对应 resource 路径（用于搜索候选）
    const TARGET_RESOURCE = {
        WORD:          'words',
        PHRASE:        'phrases',
        PARAPHRASE:    'paraphrase-groups',
        PRONUNCIATION: 'pronunciation-points',
        GRAMMAR_POINT: 'grammar-points',
    };

    // targetType 摘要字段名
    const TARGET_SUMMARY_FIELD = {
        WORD:          'word',
        PHRASE:        'phrase',
        PARAPHRASE:    'coreExpression',
        PRONUNCIATION: 'title',
        GRAMMAR_POINT: 'title',
    };

    function _getOffcanvas() {
        if (!_offcanvas) _offcanvas = new bootstrap.Offcanvas(document.getElementById('linksOffcanvas'));
        return _offcanvas;
    }

    async function open(resource, id, title) {
        _resource = resource;
        _id = id;
        document.getElementById('linksTitle').textContent = `《${title}》关联内容`;
        _clearSearch();
        _getOffcanvas().show();
        await _loadLinks();
    }

    async function _loadLinks() {
        const list = document.getElementById('linksList');
        list.innerHTML = '<div class="text-muted small py-2">加载中…</div>';
        try {
            const links = await Api.links.list(_resource, _id);
            _renderLinks(links);
        } catch (e) {
            list.innerHTML = `<div class="text-danger small">${escHtml(e.message)}</div>`;
        }
    }

    function _renderLinks(links) {
        const list = document.getElementById('linksList');
        if (!links.length) {
            list.innerHTML = '<div class="text-muted small py-2">暂无关联内容</div>';
            return;
        }
        // 按 linkType 分组
        const groups = {};
        links.forEach(l => {
            const g = l.linkType || 'vocabulary';
            if (!groups[g]) groups[g] = [];
            groups[g].push(l);
        });
        list.innerHTML = Object.entries(groups).map(([type, items]) => `
        <div class="mb-3">
          <div class="fw-semibold text-secondary small mb-1">${LINK_TYPE_LABELS[type] || type} <span class="badge bg-secondary">${items.length}</span></div>
          ${items.map(l => `
          <div class="d-flex align-items-start border rounded p-2 mb-1 bg-light gap-2">
            <div class="flex-grow-1">
              <div class="fw-semibold small">${escHtml(l.targetSummary || '')}</div>
              ${l.targetDetail ? `<div class="text-muted" style="font-size:0.8em">${escHtml(l.targetDetail)}</div>` : ''}
              ${l.note ? `<div class="text-info" style="font-size:0.8em">${escHtml(l.note)}</div>` : ''}
            </div>
            <button class="btn btn-sm btn-outline-danger py-0 px-1 flex-shrink-0" onclick="LinksPanel.removeLink('${l.linkId}')" title="删除关联"><i class="bi bi-x"></i></button>
          </div>`).join('')}
        </div>`).join('');
    }

    async function removeLink(linkId) {
        if (!confirm('确认删除此关联？')) return;
        try {
            await Api.links.remove(_resource, _id, linkId);
            showToast('关联删除成功');
            await _loadLinks();
        } catch (e) {
            showToast(e.message, 'danger');
        }
    }

    // ── 搜索候选 ──────────────────────────────
    let _searchTimer = null, _selectedTarget = null;

    function _clearSearch() {
        document.getElementById('linkTargetType').value = 'WORD';
        document.getElementById('linkSearch').value = '';
        document.getElementById('linkSearchResults').innerHTML = '';
        document.getElementById('linkType').value = 'vocabulary';
        document.getElementById('linkNote').value = '';
        _selectedTarget = null;
        document.getElementById('selectedTargetInfo').innerHTML = '';
    }

    function onSearchInput() {
        clearTimeout(_searchTimer);
        _searchTimer = setTimeout(_doSearch, 300);
    }

    async function _doSearch() {
        const q = document.getElementById('linkSearch').value.trim();
        const targetType = document.getElementById('linkTargetType').value;
        const resource = TARGET_RESOURCE[targetType];
        if (!q || !resource) { document.getElementById('linkSearchResults').innerHTML = ''; return; }
        try {
            const data = await Api.list(resource, { page: 1, size: 10, [_searchParamFor(targetType)]: q });
            _renderSearchResults(data.content, targetType);
        } catch (_) {}
    }

    function _searchParamFor(targetType) {
        // 各跨技能内容的搜索字段
        const map = { WORD: 'word', PHRASE: 'phrase', PARAPHRASE: 'groupName', PRONUNCIATION: 'title', GRAMMAR_POINT: 'title' };
        return map[targetType] || 'title';
    }

    function _renderSearchResults(items, targetType) {
        const field = TARGET_SUMMARY_FIELD[targetType];
        const el = document.getElementById('linkSearchResults');
        el.innerHTML = items.length ? items.map(item => `
        <div class="list-group-item list-group-item-action py-1 px-2 small" style="cursor:pointer"
             onclick="LinksPanel.selectTarget('${item.id}', '${escHtml(String(item[field] || ''))}')">
          ${escHtml(String(item[field] || ''))}
        </div>`).join('') : '<div class="list-group-item py-1 px-2 text-muted small">无匹配结果</div>';
    }

    function selectTarget(id, label) {
        _selectedTarget = id;
        document.getElementById('linkSearch').value = label;
        document.getElementById('linkSearchResults').innerHTML = '';
        document.getElementById('selectedTargetInfo').innerHTML =
            `<span class="badge bg-primary">${escHtml(label)}</span>`;
    }

    async function addLink() {
        if (!_selectedTarget) { showToast('请先搜索并选择关联内容', 'warning'); return; }
        const body = {
            targetType: document.getElementById('linkTargetType').value,
            targetId:   _selectedTarget,
            linkType:   document.getElementById('linkType').value || null,
            note:       document.getElementById('linkNote').value.trim() || null,
        };
        try {
            await Api.links.add(_resource, _id, body);
            showToast('关联添加成功');
            _clearSearch();
            await _loadLinks();
        } catch (e) {
            showToast(e.message, 'danger');
        }
    }

    return { open, removeLink, onSearchInput, selectTarget, addLink };
})();
