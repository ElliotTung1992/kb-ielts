// 统一 API 请求工具
const API_BASE = '/api/ielts';

async function request(method, url, body) {
    const opts = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body !== undefined) opts.body = JSON.stringify(body);
    const res = await fetch(url, opts);
    const json = await res.json();
    if (!json.success) throw new Error(json.message || '请求失败');
    return json.data;
}

const get  = (url)        => request('GET', url);
const post = (url, body)  => request('POST', url, body);
const put  = (url, body)  => request('PUT', url, body);
const del  = (url)        => request('DELETE', url);

// ── 内容管理 API ────────────────────────────
function buildListUrl(type, params = {}) {
    const q = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v !== null && v !== undefined && v !== '') q.set(k, v); });
    return `${API_BASE}/${type}?${q}`;
}

const Api = {
    dashboard: () => get(`${API_BASE}/dashboard`),
    profile: {
        get: () => get(`${API_BASE}/profile`),
        save: (data) => put(`${API_BASE}/profile`, data),
        suggestion: () => get(`${API_BASE}/profile/plan-suggestion`),
    },
    mistakes: {
        stats: (days = 30) => get(`${API_BASE}/mistakes/stats?days=${days}`),
        recent: () => get(`${API_BASE}/mistakes/recent`),
    },
    mockTests: {
        list: () => get(`${API_BASE}/mock-tests`),
        create: (data) => post(`${API_BASE}/mock-tests`, data),
        trends: () => get(`${API_BASE}/mock-tests/trends`),
    },
    writingSubmissions: {
        list: () => get(`${API_BASE}/writing-submissions`),
        create: (data) => post(`${API_BASE}/writing-submissions`, data),
    },
    speakingMaterials: {
        list: (category) => get(`${API_BASE}/speaking-materials${category ? `?category=${encodeURIComponent(category)}` : ''}`),
        create: (data) => post(`${API_BASE}/speaking-materials`, data),
    },
    training: {
        listening: (params) => get(buildListUrl('training/listening', params)),
        reading: (params) => get(buildListUrl('training/reading', params)),
        writing: (params) => get(buildListUrl('training/writing', params)),
        speaking: (params) => get(buildListUrl('training/speaking', params)),
    },
    // 通用 CRUD（type: words/phrases/paraphrase-groups/... ）
    list:        (type, params)   => get(buildListUrl(type, params)),
    getById:     (type, id)       => get(`${API_BASE}/${type}/${id}`),
    create:      (type, data)     => post(`${API_BASE}/${type}`, data),
    update:      (type, id, data) => put(`${API_BASE}/${type}/${id}`, data),
    remove:      (type, id)       => del(`${API_BASE}/${type}/${id}`),
    batchImport: (type, arr)      => post(`${API_BASE}/${type}/batch`, arr),

    // 语法练习特有
    exercisesByPoint: (grammarPointId) =>
        get(`${API_BASE}/grammar-exercises/by-grammar-point/${grammarPointId}`),

    // 内容关联 API
    links: {
        list:      (resource, id)              => get(`${API_BASE}/${resource}/${id}/links`),
        add:       (resource, id, body)        => post(`${API_BASE}/${resource}/${id}/links`, body),
        remove:    (resource, id, linkId)      => del(`${API_BASE}/${resource}/${id}/links/${linkId}`),
        backlinks: (resource, id)              => get(`${API_BASE}/${resource}/${id}/links`),
    },

    // 学习核心 API
    study: {
        todayPlan:    ()        => get(`${API_BASE}/study/today`),
        start:        (contentType, contentId) =>
            post(`${API_BASE}/study/start`, { contentType, contentId }),
        review:       (recordId, rating) =>
            post(`${API_BASE}/study/review`, { recordId, rating }),
        stats:        ()        => get(`${API_BASE}/study/stats`),
        recordsByStatus: (status) => get(`${API_BASE}/study/records?status=${status}`),
    }
};

// ── 工具函数 ────────────────────────────────
function diffBadge(d) {
    const map = { 1: ['badge-diff-1', '基础'], 2: ['badge-diff-2', '中级'], 3: ['badge-diff-3', '高级'] };
    const [cls, label] = map[d] || ['bg-secondary', '未知'];
    return `<span class="badge ${cls}">${label}</span>`;
}

function statusBadge(s) {
    const map = {
        LEARNING:  ['badge-learning',  '学习中'],
        REVIEWING: ['badge-reviewing', '复习中'],
        MASTERED:  ['badge-mastered',  '已掌握'],
    };
    const [cls, label] = map[s] || ['bg-secondary', s];
    return `<span class="badge ${cls}">${label}</span>`;
}

function escHtml(s) {
    if (!s) return '';
    return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

function showToast(msg, type = 'success') {
    const el = document.createElement('div');
    el.className = `toast align-items-center text-bg-${type} border-0 show position-fixed bottom-0 end-0 m-3`;
    el.style.zIndex = 9999;
    el.innerHTML = `<div class="d-flex"><div class="toast-body">${escHtml(msg)}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" onclick="this.closest('.toast').remove()"></button></div>`;
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 3000);
}

function fmtDate(iso) {
    if (!iso) return '-';
    return new Date(iso).toLocaleDateString('zh-CN');
}
