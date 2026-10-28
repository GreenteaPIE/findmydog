/* 보호소 입양 공고 검색 */
(function () {
  const { api, escapeHtml } = window.FMD;
  const form = document.getElementById('adopt-form');
  const grid = document.getElementById('adopt-grid');
  const count = document.getElementById('adopt-count');
  const pages = document.getElementById('adopt-pages');
  const sido = document.getElementById('sido'), sigungu = document.getElementById('sigungu'), breed = document.getElementById('breed');
  const from = document.getElementById('from'), to = document.getElementById('to');
  let regions = [];

  const iso = (d) => d.toISOString().slice(0, 10);
  const today = new Date();
  to.value = iso(today);
  from.value = iso(new Date(today.getTime() - 30 * 86400000));
  to.max = from.max = iso(today);

  // 주소의 조건을 폼에 되살린다 (뒤로 가기·공유용)
  const params = new URLSearchParams(location.search);

  api('/api/adoption/codes').then((codes) => {
    regions = codes.regions;
    sido.insertAdjacentHTML('beforeend', regions.map((r) => `<option value="${r.code}">${escapeHtml(r.name)}</option>`).join(''));
    breed.insertAdjacentHTML('beforeend', codes.breeds.map((b) => `<option value="${b.code}">${escapeHtml(b.name)}</option>`).join(''));
    ['from', 'to', 'sido', 'breed'].forEach((k) => { if (params.get(k)) form.elements[k].value = params.get(k); });
    fillSigungu();
    if (params.get('sigungu')) sigungu.value = params.get('sigungu');
    search(Number(params.get('page')) || 1, false);
  });

  function fillSigungu() {
    const region = regions.find((r) => r.code === sido.value);
    sigungu.innerHTML = '<option value="">전체</option>' + (region ? region.sigungu.map((s) => `<option value="${s.code}">${escapeHtml(s.name)}</option>`).join('') : '');
    sigungu.disabled = !region;
  }
  sido.addEventListener('change', fillSigungu);
  form.addEventListener('submit', (e) => { e.preventDefault(); search(1, true); });

  function card(a) {
    const rows = [['성별', a.sex], ['나이', a.age], ['체중', a.weight], ['중성화', a.neuter], ['발견', [a.happenDate, a.happenPlace].filter(Boolean).join(' ')], ['보호소', a.careName], ['연락처', a.careTel]]
      .filter(([, v]) => v).map(([k, v]) => `<dt>${k}</dt><dd>${escapeHtml(v)}</dd>`).join('');
    return `<article class="animal">
      <div class="animal__photo">${a.imageUrl ? `<img src="${escapeHtml(a.imageUrl)}" alt="${escapeHtml(a.breed)} 사진" loading="lazy" referrerpolicy="no-referrer">` : ''}</div>
      <div class="animal__body">
        <div>${a.state ? `<span class="badge">${escapeHtml(a.state)}</span>` : ''}</div>
        <h3>${escapeHtml(a.breed || '품종 미상')}${a.color ? `, ${escapeHtml(a.color)}` : ''}</h3>
        <dl>${rows}</dl>
        ${a.specialMark ? `<p style="font-size:14px;color:var(--ink-2)">${escapeHtml(a.specialMark)}</p>` : ''}
      </div>
      <div class="animal__foot">
        ${a.careTel ? `<a class="btn btn--outline btn--sm" href="tel:${escapeHtml(a.careTel)}">보호소 전화</a>` : ''}
        ${a.detailUrl ? `<a class="btn btn--ink btn--sm" href="${escapeHtml(a.detailUrl)}" target="_blank" rel="noopener">공고 원문</a>` : ''}
      </div>
    </article>`;
  }

  function renderPages(result) {
    pages.innerHTML = '';
    if (result.page <= 1 && !result.hasNext) return;
    const add = (label, page, current) => {
      const el = document.createElement(page ? 'a' : 'span');
      el.textContent = label;
      if (page) { el.href = '#'; el.addEventListener('click', (e) => { e.preventDefault(); search(page, true); }); }
      else el.className = 'is-disabled';
      if (current) el.setAttribute('aria-current', 'page');
      pages.append(el);
    };
    add('‹ 이전', result.page > 1 ? result.page - 1 : 0);
    add(String(result.page), 0, true);
    add('다음 ›', result.hasNext ? result.page + 1 : 0);
  }

  async function search(page, push) {
    const query = new URLSearchParams(new FormData(form));
    [...query.keys()].forEach((k) => { if (!query.get(k)) query.delete(k); });
    query.set('page', page);
    if (push) history.replaceState(null, '', `?${query}`);
    grid.innerHTML = Array.from({ length: 6 }, () => '<div class="skeleton"></div>').join('');
    count.textContent = '공고를 찾는 중이에요…';
    pages.innerHTML = '';
    try {
      const result = await api(`/api/adoption?${query}`);
      count.textContent = result.items.length
        ? (result.totalCount >= 0 ? `공고 ${result.totalCount.toLocaleString()}건` : `공고 ${result.items.length}건 (${result.page}쪽)`)
        : '';
      grid.innerHTML = result.items.length ? result.items.map(card).join('')
        : '<div class="empty" style="grid-column:1/-1"><img src="/img/dog-husky.png" alt=""><strong>조건에 맞는 공고가 없어요</strong><p>날짜를 넓히거나 시군구를 전체로 바꿔 보세요.</p></div>';
      renderPages(result);
      if (push) count.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (e) {
      count.textContent = '';
      grid.innerHTML = `<div class="empty" style="grid-column:1/-1"><strong>공고를 불러오지 못했어요</strong><p>${escapeHtml(e.message)}</p><button class="btn btn--outline" type="button">다시 시도</button></div>`;
      grid.querySelector('button').addEventListener('click', () => search(page, false));
    }
  }
})();
