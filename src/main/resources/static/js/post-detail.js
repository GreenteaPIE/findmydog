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
      mapEl.__map = map; // 아래 목격 제보 코드가 같은 지도를 쓴다
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

/* 목격 제보와 신고 */
(function () {
  const { api, toast, whenKakao } = window.FMD;
  const $ = (id) => document.getElementById(id);
  const sightings = window.SIGHTINGS || [];
  const mapEl = document.getElementById('detail-map');

  // 지도: 목격 위치에 번호 표시, 목격 제보를 쓰는 중에는 지도를 눌러 위치 고르기
  if (mapEl) {
    whenKakao(mapEl, () => {
      const map = mapEl.__map; // 위에서 만든 상세 지도
      if (!map) return;
      const points = sightings.filter((s) => s.lat && s.lng);
      const bounds = new kakao.maps.LatLngBounds();
      bounds.extend(new kakao.maps.LatLng(Number(mapEl.dataset.lat), Number(mapEl.dataset.lng)));
      points.forEach((s, i) => {
        const pos = new kakao.maps.LatLng(s.lat, s.lng);
        bounds.extend(pos);
        const el = document.createElement('a');
        el.className = 'sighting-pin';
        el.href = `#sighting-${s.id}`;
        el.textContent = String(sightings.length - sightings.indexOf(s));
        new kakao.maps.CustomOverlay({ map, position: pos, content: el, yAnchor: 0.5 });
      });
      if (points.length) map.setBounds(bounds, 40, 40, 40, 40);

      let pick;
      kakao.maps.event.addListener(map, 'click', (e) => {
        const details = document.querySelector('.sighting-form');
        if (!details?.open) return;
        $('s-lat').value = e.latLng.getLat().toFixed(6);
        $('s-lng').value = e.latLng.getLng().toFixed(6);
        if (!pick) pick = new kakao.maps.Marker({ map });
        pick.setPosition(e.latLng);
        $('s-picked').textContent = '본 곳을 지도에 표시했어요. 다시 누르면 옮겨져요.';
      });
    });
  }

  // 목격 제보 남기기
  const form = $('sighting-form');
  if (form) {
    const pad = (n) => String(n).padStart(2, '0');
    const d = new Date();
    const now = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    $('s-seenAt').value = now;
    $('s-seenAt').max = now;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const alertBox = $('s-alert');
      alertBox.hidden = true;
      const body = {
        seenAt: $('s-seenAt').value,
        place: $('s-place').value.trim(),
        note: $('s-note').value.trim() || null,
        contact: $('s-contact').value.trim() || null,
        latitude: $('s-lat').value ? Number($('s-lat').value) : null,
        longitude: $('s-lng').value ? Number($('s-lng').value) : null,
      };
      if (!body.seenAt || !body.place) {
        alertBox.textContent = '본 시간과 본 곳을 입력해 주세요.';
        alertBox.hidden = false;
        return;
      }
      const button = form.querySelector('[type=submit]');
      button.disabled = true;
      try {
        const result = await api(form.dataset.url, { method: 'POST', body });
        location.hash = `sighting-${result.id}`;
        location.reload();
      } catch (err) {
        alertBox.textContent = err.message;
        alertBox.hidden = false;
        button.disabled = false;
      }
    });
  }

  document.querySelectorAll('[data-delete-sighting]').forEach((button) => button.addEventListener('click', async () => {
    if (!confirm('이 목격 제보를 지울까요?')) return;
    try {
      await api(`/api/v1/sightings/${button.dataset.deleteSighting}`, { method: 'DELETE' });
      location.reload();
    } catch (err) {
      toast(err.message, 'error');
    }
  }));

  // 신고
  const dialog = $('report-dialog');
  document.querySelector('[data-open-report]')?.addEventListener('click', () => dialog.showModal());
  document.querySelector('[data-close-report]')?.addEventListener('click', () => dialog.close());
  $('report-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const reason = e.target.querySelector('input[name=reason]:checked')?.value;
    if (!reason) return toast('신고 이유를 골라 주세요.', 'error');
    try {
      const result = await api(e.target.dataset.url, { method: 'POST', body: { reason, detail: $('report-detail').value.trim() || null } });
      dialog.close();
      toast(result.hidden ? '신고가 쌓여 이 제보를 숨겼어요. 관리자가 확인할 거예요.' : '신고했어요. 관리자가 확인할 거예요.');
    } catch (err) {
      dialog.close();
      toast(err.message, 'error');
    }
  });
})();
