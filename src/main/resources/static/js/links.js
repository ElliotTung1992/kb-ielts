/**
 * 关联管理通用逻辑（技能内容页复用）
 *
 * Offcanvas 模式：LinksPanel.open(resource, id, title)
 * 内嵌模式：     LinksPanel.initInline(resource, id, containerId)
 */
const LinksPanel = (() => {
    let _resource = null, _id = null;
    let _offcanvas = null;

    // targetType 分组展示标签
    const TARGET_TYPE_LABELS = {
        WORD:          '词汇',
        PHRASE:        '短语',
        PARAPHRASE:    '同义替换',
        PRONUNCIATION: '发音',
        GRAMMAR_POINT: '语法要点',
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
        // 按 targetType 分组
        const groups = {};
        links.forEach(l => {
            const g = l.targetType || 'WORD';
            if (!groups[g]) groups[g] = [];
            groups[g].push(l);
        });
        list.innerHTML = Object.entries(groups).map(([type, items]) => `
        <div class="mb-3">
          <div class="fw-semibold text-secondary small mb-1">${TARGET_TYPE_LABELS[type] || type} <span class="badge bg-secondary">${items.length}</span></div>
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

    // ── 内嵌模式 ──────────────────────────────
    function initInline(resource, id, containerId) {
        _resource = resource;
        _id = id;
        const container = document.getElementById(containerId);
        if (!container) return;
        const enabled = id !== null;
        container.innerHTML = `
        <hr class="my-3">
        <div class="fw-semibold mb-2 text-secondary small">── 关联内容 ──</div>
        <div class="border rounded p-2 bg-light mb-2${enabled ? '' : ' opacity-50 pe-none'}">
          <div class="row g-2 mb-2">
            <div class="col-md-4">
              <label class="form-label form-label-sm mb-1">内容类型</label>
              <select class="form-select form-select-sm" id="linkTargetType" ${enabled ? '' : 'disabled'}
                      onchange="document.getElementById('linkSearch').value='';document.getElementById('linkSearchResults').innerHTML=''">
                <option value="WORD">单词</option>
                <option value="PHRASE">短语</option>
                <option value="PARAPHRASE">同义替换</option>
                <option value="PRONUNCIATION">发音要点</option>
                <option value="GRAMMAR_POINT">语法要点</option>
              </select>
            </div>
            <div class="col-md-5 position-relative">
              <label class="form-label form-label-sm mb-1">搜索</label>
              <input class="form-control form-control-sm" id="linkSearch" placeholder="输入关键词搜索"
                     ${enabled ? 'oninput="LinksPanel.onSearchInput()"' : 'disabled'}>
              <div id="linkSearchResults" class="list-group position-absolute w-100" style="z-index:1060;top:100%"></div>
            </div>
            <div class="col-md-3">
              <label class="form-label form-label-sm mb-1">备注</label>
              <input class="form-control form-control-sm" id="linkNote" placeholder="补充说明" ${enabled ? '' : 'disabled'}>
            </div>
          </div>
          <div id="selectedTargetInfo" class="mb-2"></div>
          <button class="btn btn-primary btn-sm w-100" onclick="LinksPanel.addLink()" ${enabled ? '' : 'disabled'}>+ 添加</button>
        </div>
        ${enabled
            ? '<div id="linksList"><div class="text-muted small py-2">加载中…</div></div>'
            : '<div class="text-muted small text-center py-2 border rounded">保存后可在此添加关联</div>'}`;
        if (enabled) {
            _loadLinks();
        }
    }

    return { open, removeLink, onSearchInput, selectTarget, addLink, initInline };
})();
