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
          ${items.map(_renderLinkItem).join('')}
        </div>`).join('');
    }

    function _renderLinkItem(link) {
        const detail = link.targetDetail ? _compact(link.targetDetail, 96) : '';
        return `
        <div class="d-flex align-items-center border rounded px-2 py-1 mb-1 bg-white gap-2">
          <div class="d-flex align-items-center gap-2 flex-wrap flex-grow-1 min-width-0">
            <span class="badge bg-light text-dark border flex-shrink-0">${TARGET_TYPE_LABELS[link.targetType] || link.targetType}</span>
            <span class="fw-semibold small">${escHtml(link.targetSummary || '')}</span>
            ${detail ? `<span class="text-muted small text-truncate" style="max-width:360px">${escHtml(detail)}</span>` : ''}
            ${link.note ? `<span class="badge bg-info-subtle text-info-emphasis border border-info-subtle flex-shrink-0">${escHtml(link.note)}</span>` : ''}
          </div>
          <button class="btn btn-sm btn-outline-danger py-0 px-1 flex-shrink-0" onclick="LinksPanel.removeLink('${link.linkId}', '${link.targetType}', '${link.targetId}')" title="删除关联"><i class="bi bi-x"></i></button>
        </div>`;
    }

    function _compact(value, limit) {
        const text = String(value || '').trim();
        return text.length > limit ? text.substring(0, limit) + '...' : text;
    }

    async function removeLink(linkId, targetType, targetId) {
        if (!confirm('确认删除此关联？')) return;
        try {
            await Api.links.remove(_resource, _id, linkId);
            await _removeInheritedTopicTags(targetType, targetId);
            showToast('关联删除成功');
            await _loadLinks();
        } catch (e) {
            showToast(e.message, 'danger');
        }
    }

    // ── 搜索候选 ──────────────────────────────
    let _searchTimer = null, _selectedTarget = null, _lastSearchHadResults = false;

    function _clearSearch() {
        document.getElementById('linkTargetType').value = 'WORD';
        document.getElementById('linkSearch').value = '';
        document.getElementById('linkSearchResults').innerHTML = '';
        document.getElementById('linkNote').value = '';
        _selectedTarget = null;
        _lastSearchHadResults = false;
        document.getElementById('selectedTargetInfo').innerHTML = '';
        _syncCreatePanel();
    }

    function onSearchInput() {
        clearTimeout(_searchTimer);
        _selectedTarget = null;
        document.getElementById('selectedTargetInfo').innerHTML = '';
        _syncCreatePanel();
        _searchTimer = setTimeout(_doSearch, 300);
    }

    async function _doSearch() {
        const q = document.getElementById('linkSearch').value.trim();
        const targetType = document.getElementById('linkTargetType').value;
        const resource = TARGET_RESOURCE[targetType];
        _syncCreatePanel();
        if (!q || !resource) {
            _lastSearchHadResults = false;
            document.getElementById('linkSearchResults').innerHTML = '';
            return;
        }
        try {
            const data = await Api.list(resource, { page: 1, size: 10, [_searchParamFor(targetType)]: q });
            _renderSearchResults(data.content, targetType);
        } catch (_) {}
    }

    function _searchParamFor(targetType) {
        const map = { WORD: 'keyword', PHRASE: 'keyword', PARAPHRASE: 'groupName', PRONUNCIATION: 'title', GRAMMAR_POINT: 'title' };
        return map[targetType] || 'title';
    }

    function _renderSearchResults(items, targetType) {
        const field = TARGET_SUMMARY_FIELD[targetType];
        const el = document.getElementById('linkSearchResults');
        _lastSearchHadResults = items.length > 0;
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
        _syncCreatePanel();
    }

    async function addLink() {
        if (!_selectedTarget) {
            const targetType = document.getElementById('linkTargetType').value;
            const keyword = document.getElementById('linkSearch').value.trim();
            if (_canQuickCreate(targetType) && keyword && !_lastSearchHadResults) {
                await createAndLink();
                return;
            }
            showToast(_canQuickCreate(targetType) && keyword
                ? '请选择已有结果，或使用下方“新建并关联”'
                : '请先搜索并选择关联内容', 'warning');
            return;
        }
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

    // ── 新建并关联（目前支持单词 / 短语） ───────────────
    function _canQuickCreate(targetType) {
        return targetType === 'WORD' || targetType === 'PHRASE';
    }

    function _syncCreatePanel() {
        const panel = document.getElementById('quickCreatePanel');
        if (!panel) return;
        const targetType = document.getElementById('linkTargetType').value;
        const keyword = document.getElementById('linkSearch').value.trim();
        const enabled = _canQuickCreate(targetType) && keyword && !_selectedTarget;
        panel.classList.toggle('d-none', !enabled);
        if (!enabled) return;
        document.getElementById('quickCreateName').textContent = keyword;
        document.getElementById('quickCreateType').textContent = targetType === 'WORD' ? '单词' : '短语';
        document.getElementById('quickCreateMeaning').placeholder = targetType === 'WORD' ? '中文释义' : '中文含义';
        _fillQuickCreateTags();
    }

    function _sourceSkillTag() {
        const map = {
            'speaking-topics': 'speaking',
            'listening-items': 'listening',
            'reading-items': 'reading',
            'writing-tasks': 'writing',
        };
        return map[_resource] || null;
    }

    function _sourceTopicTags() {
        const candidateIds = [
            'topicTags',
            'itemTags',
            'taskTags',
            'filterTopic',
        ];
        for (const id of candidateIds) {
            const el = document.getElementById(id);
            const value = el?.value?.trim();
            if (value) return value;
        }
        return null;
    }

    function _fillQuickCreateTags() {
        const input = document.getElementById('quickCreateTags');
        if (!input || input.value.trim()) return;
        const tags = _sourceTopicTags();
        if (tags) input.value = tags;
    }

    function _buildQuickCreateBody(targetType, keyword) {
        const meaning = document.getElementById('quickCreateMeaning').value.trim() || null;
        const difficulty = parseInt(document.getElementById('quickCreateDifficulty').value, 10);
        _fillQuickCreateTags();
        const topicTags = document.getElementById('quickCreateTags').value.trim() || null;
        const skillTags = _sourceSkillTag();
        if (targetType === 'WORD') {
            return {
                word: keyword,
                definitionZh: meaning,
                difficulty,
                skillTags,
                topicTags,
                wordList: 'IELTS',
            };
        }
        return {
            phrase: keyword,
            meaningZh: meaning,
            category: 'sentence-frame',
            difficulty,
            skillTags,
            topicTags,
        };
    }

    async function _removeInheritedTopicTags(targetType, targetId) {
        if (!_canQuickCreate(targetType) || !targetId) return;
        const sourceTags = _splitTags(_sourceTopicTags());
        if (!sourceTags.length) return;
        const resource = TARGET_RESOURCE[targetType];
        const item = await Api.getById(resource, targetId);
        const currentTags = _splitTags(item.topicTags);
        if (!currentTags.length) return;
        const sourceTagSet = new Set(sourceTags.map(tag => tag.toLowerCase()));
        const nextTags = currentTags.filter(tag => !sourceTagSet.has(tag.toLowerCase()));
        if (nextTags.length === currentTags.length) return;
        await Api.update(resource, targetId, {
            ...item,
            topicTags: nextTags.length ? nextTags.join(',') : null,
        });
    }

    function _splitTags(value) {
        if (!value) return [];
        return String(value).split(',').map(tag => tag.trim()).filter(Boolean);
    }

    async function createAndLink() {
        if (!_id) {
            showToast('请先保存当前内容，再新增关联', 'warning');
            return;
        }
        const targetType = document.getElementById('linkTargetType').value;
        const keyword = document.getElementById('linkSearch').value.trim();
        if (!_canQuickCreate(targetType) || !keyword) {
            showToast('当前只支持新建单词或短语', 'warning');
            return;
        }
        const resource = TARGET_RESOURCE[targetType];
        const body = _buildQuickCreateBody(targetType, keyword);
        try {
            const created = await Api.create(resource, body);
            await Api.links.add(_resource, _id, {
                targetType,
                targetId: created.id,
                note: document.getElementById('linkNote').value.trim() || null,
            });
            showToast('已新建并关联');
            _clearSearch();
            document.getElementById('quickCreateMeaning').value = '';
            document.getElementById('quickCreateTags').value = '';
            document.getElementById('quickCreateDifficulty').value = '1';
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
                      onchange="document.getElementById('linkSearch').value='';document.getElementById('linkSearchResults').innerHTML='';document.getElementById('selectedTargetInfo').innerHTML='';LinksPanel.syncCreatePanel()">
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
          <div id="quickCreatePanel" class="border-top mt-3 pt-3 d-none">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <div class="small text-muted">未找到时新建<span id="quickCreateType">单词</span></div>
              <span class="badge bg-light text-dark border" id="quickCreateName"></span>
            </div>
            <div class="row g-2">
              <div class="col-md-5">
                <input class="form-control form-control-sm" id="quickCreateMeaning" placeholder="中文释义">
              </div>
              <div class="col-md-3">
                <select class="form-select form-select-sm" id="quickCreateDifficulty">
                  <option value="1">基础</option>
                  <option value="2">中级</option>
                  <option value="3">高级</option>
                </select>
              </div>
              <div class="col-md-4">
                <input class="form-control form-control-sm" id="quickCreateTags" placeholder="标签">
              </div>
            </div>
            <button class="btn btn-outline-primary btn-sm w-100 mt-2" onclick="LinksPanel.createAndLink()">新建并关联</button>
          </div>
        </div>
        ${enabled
            ? '<div id="linksList"><div class="text-muted small py-2">加载中…</div></div>'
            : '<div class="text-muted small text-center py-2 border rounded">保存后可在此添加关联</div>'}`;
        if (enabled) {
            _loadLinks();
            _syncCreatePanel();
        }
    }

    return { open, removeLink, onSearchInput, selectTarget, addLink, initInline, createAndLink, syncCreatePanel: _syncCreatePanel };
})();
