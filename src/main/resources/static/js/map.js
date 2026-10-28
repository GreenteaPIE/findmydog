/* 제보 지도: 핀, 묶음(클러스터), 화면 안 목록 */
(function () {
  const { api, whenKakao, escapeHtml } = window.FMD;
  const mapEl = document.getElementById('board-map');
  const listEl = document.getElementById('map-items');
  const countEl = document.getElementById('map-count');
  const hideReunited = document.getElementById('map-hide-reunited');
  let posts = [], kind = '', map, clusterer, info, markers = new Map();

  const visible = () => posts.filter((p) => (!kind || p.kind === kind) && (!hideReunited.checked || !p.reunited));
  const label = (p) => p.reunited ? '찾았어요' : p.kind === '발견' ? '발견' : '실종';
  const date = (iso) => iso ? `${Number(iso.slice(5, 7))}월 ${Number(iso.slice(8, 10))}일` : '';

  function renderList() {
    let items = visible();
    if (map) {
      const bounds = map.getBounds();
      items = items.filter((p) => bounds.contain(new kakao.maps.LatLng(p.lat, p.lng)));
    }
    countEl.textContent = map ? `지도 안에 제보 ${items.length}건` : `제보 ${items.length}건`;
    listEl.innerHTML = items.length ? items.map((p) => `
      <li><button type="button" class="map-item" data-id="${p.id}">
        ${p.thumbnail ? `<img src="${p.thumbnail}" alt="" loading="lazy">` : '<span class="noimg"></span>'}
        <span><span class="badge badge--${p.reunited ? 'home' : p.kind === '발견' ? 'found' : 'lost'}">${label(p)}</span>
        <strong>${escapeHtml(p.title)}</strong>
        <small>${escapeHtml([p.breed, p.address, date(p.lostDate)].filter(Boolean).join(', '))}</small></span>
      </button></li>`).join('')
      : '<li style="padding:16px;color:var(--ink-2);font-size:14px">이 근처에는 제보가 없어요. 지도를 넓혀 보세요.</li>';
    listEl.querySelectorAll('.map-item').forEach((b) => b.addEventListener('click', () => focus(Number(b.dataset.id))));
  }

  function popup(p) {
    return `<div class="map-pop">${p.thumbnail ? `<img src="${p.thumbnail}" alt="">` : ''}
      <span class="badge badge--${p.reunited ? 'home' : p.kind === '발견' ? 'found' : 'lost'}">${label(p)}</span>
      <div><a href="/posts/${p.id}">${escapeHtml(p.title)}</a></div>
      <div style="color:#5a6475">${escapeHtml([p.breed, date(p.lostDate)].filter(Boolean).join(', '))}</div></div>`;
  }

  function focus(id) {
    const p = posts.find((x) => x.id === id);
    const marker = markers.get(id);
    if (!p || !marker || !map) { location.href = `/posts/${id}`; return; }
    map.setLevel(Math.min(map.getLevel(), 4));
    map.panTo(marker.getPosition());
    info.setContent(popup(p));
    info.open(map, marker);
  }

  function drawMarkers() {
    clusterer.clear();
    markers = new Map();
    const lost = new kakao.maps.MarkerImage('/img/marker-lost.png', new kakao.maps.Size(30, 35));
    const found = new kakao.maps.MarkerImage('/img/marker-found.png', new kakao.maps.Size(30, 35));
    const list = visible().map((p) => {
      const m = new kakao.maps.Marker({ position: new kakao.maps.LatLng(p.lat, p.lng), image: p.kind === '발견' ? found : lost, title: p.title });
      kakao.maps.event.addListener(m, 'click', () => focus(p.id));
      markers.set(p.id, m);
      return m;
    });
    clusterer.addMarkers(list);
    renderList();
  }

  document.querySelectorAll('#map-filter a').forEach((a) => a.addEventListener('click', (e) => {
    e.preventDefault();
    kind = a.dataset.kind;
    document.querySelectorAll('#map-filter a').forEach((x) => x.setAttribute('aria-current', String(x === a)));
    map ? drawMarkers() : renderList();
  }));
  hideReunited.addEventListener('change', () => (map ? drawMarkers() : renderList()));

  api('/api/map/posts').then((data) => {
    posts = data;
    renderList();
    whenKakao(mapEl, () => {
      map = new kakao.maps.Map(mapEl, { center: new kakao.maps.LatLng(37.5665, 126.978), level: 9 });
      map.addControl(new kakao.maps.ZoomControl(), kakao.maps.ControlPosition.RIGHT);
      clusterer = new kakao.maps.MarkerClusterer({ map, averageCenter: true, minLevel: 6 });
      info = new kakao.maps.InfoWindow({ removable: true });
      kakao.maps.event.addListener(map, 'idle', renderList);
      // 가장 최근 제보가 있는 곳에서 시작하고, 위치 권한을 주면 내 주변으로 옮긴다
      if (posts.length) map.setCenter(new kakao.maps.LatLng(posts[0].lat, posts[0].lng));
      navigator.geolocation?.getCurrentPosition((p) => { map.setCenter(new kakao.maps.LatLng(p.coords.latitude, p.coords.longitude)); map.setLevel(6); }, () => {}, { timeout: 8000 });
      drawMarkers();
    });
  }).catch((e) => { countEl.textContent = e.message; });
})();
