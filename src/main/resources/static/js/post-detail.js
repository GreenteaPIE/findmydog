/* 제보 상세: 사진 넘겨 보기, 크게 보기, 지도, 공유, 글쓴이 도구 */
(function () {
  const { api, toast, whenKakao } = window.FMD;

  // 사진
  const main = document.querySelector('[data-gallery-main]');
  document.querySelectorAll('.gallery__thumbs button').forEach((button) => {
    button.addEventListener('click', () => {
      main.src = button.dataset.src;
      document.querySelectorAll('.gallery__thumbs button').forEach((b) => b.setAttribute('aria-current', String(b === button)));
    });
  });
  main?.addEventListener('click', () => {
    const box = document.createElement('div');
    box.className = 'lightbox';
    box.setAttribute('role', 'dialog');
    box.setAttribute('aria-label', '사진 크게 보기');
    box.innerHTML = '<button type="button" aria-label="닫기">×</button>';
    const img = new Image();
    img.src = main.src;
    img.alt = main.alt;
    box.append(img);
    const close = () => { box.remove(); document.removeEventListener('keydown', onKey); main.focus(); };
    const onKey = (e) => { if (e.key === 'Escape') close(); };
    box.addEventListener('click', close);
    document.addEventListener('keydown', onKey);
    document.body.append(box);
    box.querySelector('button').focus();
  });
  if (main) main.tabIndex = 0;
  main?.addEventListener('keydown', (e) => { if (e.key === 'Enter') main.click(); });

  // 지도
  const mapEl = document.getElementById('detail-map');
  if (mapEl) {
    whenKakao(mapEl, () => {
      const pos = new kakao.maps.LatLng(Number(mapEl.dataset.lat), Number(mapEl.dataset.lng));
      const map = new kakao.maps.Map(mapEl, { center: pos, level: 4 });
      const icon = mapEl.dataset.kind === '발견' ? '/img/marker-found.png' : '/img/marker-lost.png';
      new kakao.maps.Marker({ map, position: pos, image: new kakao.maps.MarkerImage(icon, new kakao.maps.Size(36, 42)) });
      map.addControl(new kakao.maps.ZoomControl(), kakao.maps.ControlPosition.RIGHT);
    });
  }

  // 공유
  document.querySelector('[data-share]')?.addEventListener('click', async (e) => {
    const data = { title: e.currentTarget.dataset.title, url: location.href };
    try {
      if (navigator.share) await navigator.share(data);
      else { await navigator.clipboard.writeText(location.href); toast('링크를 복사했어요. 단톡방이나 SNS에 붙여 넣어 주세요.'); }
    } catch (err) {
      if (err.name !== 'AbortError') toast('링크를 복사하지 못했어요. 주소창의 주소를 복사해 주세요.', 'error');
    }
  });

  // 글쓴이 도구
  document.querySelector('[data-status-url]')?.addEventListener('click', async (e) => {
    const button = e.currentTarget;
    button.disabled = true;
    try {
      await api(button.dataset.statusUrl, { method: 'PATCH', body: { status: button.dataset.status } });
      location.reload();
    } catch (err) {
      toast(err.message, 'error');
      button.disabled = false;
    }
  });
  document.querySelector('[data-delete-url]')?.addEventListener('click', async (e) => {
    if (!confirm('이 제보와 사진을 지울까요? 지운 제보는 되돌릴 수 없어요.')) return;
    try {
      await api(e.currentTarget.dataset.deleteUrl, { method: 'DELETE' });
      location.href = '/my/posts';
    } catch (err) {
      toast(err.message, 'error');
    }
  });
})();
