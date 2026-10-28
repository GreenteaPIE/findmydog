/* 모든 화면 공통: API 호출(CSRF), 알림, 메뉴, 카카오 지도 준비 */
(function () {
  const meta = (name) => document.querySelector(`meta[name="${name}"]`)?.content;

  /** JSON API 호출. 실패하면 서버가 준 message 를 담은 Error 를 던진다 */
  async function api(url, { method = 'GET', body, headers = {} } = {}) {
    const opts = { method, headers: { Accept: 'application/json', ...headers }, credentials: 'same-origin' };
    const token = meta('_csrf'), header = meta('_csrf_header');
    if (token && header && method !== 'GET') opts.headers[header] = token;
    if (body instanceof FormData) opts.body = body;
    else if (body !== undefined) { opts.body = JSON.stringify(body); opts.headers['Content-Type'] = 'application/json'; }

    const res = await fetch(url, opts);
    const data = res.status === 204 ? null : await res.json().catch(() => null);
    if (!res.ok) {
      const message = res.status === 401 ? '로그인이 필요해요. 다시 로그인해 주세요.'
        : res.status === 403 && !data?.message ? '권한이 없거나 페이지가 오래됐어요. 새로고침한 뒤 다시 시도해 주세요.'
        : data?.message || `요청을 처리하지 못했어요 (${res.status})`;
      const error = new Error(message);
      error.status = res.status;
      error.fields = data?.fields || {};
      throw error;
    }
    return data;
  }

  let toastTimer;
  function toast(message, type) {
    let el = document.querySelector('.toast');
    if (!el) {
      el = document.createElement('div');
      el.className = 'toast';
      el.setAttribute('role', 'status');
      document.body.append(el);
    }
    el.textContent = message;
    el.classList.toggle('toast--error', type === 'error');
    el.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { el.hidden = true; }, 3200);
  }

  const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

  /** 카카오 지도 SDK 가 준비되면 callback. 키가 없거나 막히면 안내 문구를 보여 준다 */
  function whenKakao(container, callback) {
    const fail = () => {
      const box = container.querySelector('.map-fallback');
      if (box) box.textContent = '지도를 불러오지 못했어요. 카카오 지도 키와 사이트 도메인 등록을 확인해 주세요.';
    };
    if (!window.kakao?.maps?.load) return fail();
    window.kakao.maps.load(() => {
      container.querySelector('.map-fallback')?.remove();
      callback();
    });
  }

  window.FMD = { api, toast, escapeHtml, whenKakao };

  // 모바일 메뉴
  const toggle = document.querySelector('.menu-toggle');
  const nav = document.getElementById('site-nav');
  toggle?.addEventListener('click', () => {
    const open = nav.classList.toggle('is-open');
    toggle.setAttribute('aria-expanded', String(open));
    toggle.setAttribute('aria-label', open ? '메뉴 닫기' : '메뉴 열기');
  });

  // 사용자 메뉴
  const userButton = document.querySelector('.user-menu > button');
  const panel = document.getElementById('user-menu-panel');
  if (userButton && panel) {
    const close = () => { panel.hidden = true; userButton.setAttribute('aria-expanded', 'false'); };
    userButton.addEventListener('click', (e) => {
      e.stopPropagation();
      panel.hidden = !panel.hidden;
      userButton.setAttribute('aria-expanded', String(!panel.hidden));
    });
    document.addEventListener('click', (e) => { if (!panel.contains(e.target)) close(); });
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape') close(); });
  }

  // 회원 탈퇴
  document.querySelector('[data-delete-account]')?.addEventListener('click', async () => {
    if (!confirm('탈퇴하면 내가 쓴 제보와 사진이 모두 지워져요. 탈퇴할까요?')) return;
    try {
      await api('/api/v1/user', { method: 'DELETE' });
      alert('탈퇴했어요. 그동안 고마웠어요.');
      location.href = '/';
    } catch (e) {
      toast(e.message, 'error');
    }
  });
})();
