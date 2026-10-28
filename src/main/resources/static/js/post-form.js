/* 제보 쓰기·고치기: 지도에서 위치 고르기, 사진 고르기, 저장 */
(function () {
  const { api, toast, whenKakao, escapeHtml } = window.FMD;
  const form = document.getElementById('post-form');
  const mode = form.dataset.mode;
  const maxImages = Number(form.dataset.maxImages || 5);
  const $ = (id) => document.getElementById(id);

  // ---------- 일시: 미래는 고를 수 없게, 새 글은 지금으로 ----------
  const pad = (n) => String(n).padStart(2, '0');
  const now = new Date();
  const localNow = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`;
  $('lostDate').max = localNow;
  if (!$('lostDate').value) $('lostDate').value = localNow;

  // ---------- 지도 ----------
  const mapEl = $('pick-map');
  let map, marker, geocoder, places;

  function setPosition(latlng, label) {
    $('latitude').value = latlng.getLat().toFixed(6);
    $('longitude').value = latlng.getLng().toFixed(6);
    marker.setPosition(latlng);
    marker.setMap(map);
    clearError('latitude');
    const show = (text) => {
      $('address').value = text || '';
      $('picked').innerHTML = `선택한 곳: <b>${escapeHtml(text || '지도에 표시한 곳')}</b>`;
    };
    if (label) return show(label);
    geocoder.coord2RegionCode(latlng.getLng(), latlng.getLat(), (result, status) => {
      const region = status === kakao.maps.services.Status.OK && (result.find((r) => r.region_type === 'H') || result[0]);
      show(region ? region.address_name : '');
    });
  }

  whenKakao(mapEl, () => {
    const lat = Number($('latitude').value), lng = Number($('longitude').value);
    const hasPoint = lat && lng;
    const center = hasPoint ? new kakao.maps.LatLng(lat, lng) : new kakao.maps.LatLng(37.5665, 126.978);
    map = new kakao.maps.Map(mapEl, { center, level: hasPoint ? 4 : 6 });
    map.addControl(new kakao.maps.ZoomControl(), kakao.maps.ControlPosition.RIGHT);
    marker = new kakao.maps.Marker({ position: center });
    if (hasPoint) marker.setMap(map);
    geocoder = new kakao.maps.services.Geocoder();
    places = new kakao.maps.services.Places();
    kakao.maps.event.addListener(map, 'click', (e) => setPosition(e.latLng));
  });

  $('my-location').addEventListener('click', () => {
    if (!map) return toast('지도를 불러오지 못해 위치를 고를 수 없어요.', 'error');
    if (!navigator.geolocation) return toast('이 브라우저는 현재 위치를 지원하지 않아요.', 'error');
    navigator.geolocation.getCurrentPosition(
      (p) => { const ll = new kakao.maps.LatLng(p.coords.latitude, p.coords.longitude); map.setCenter(ll); map.setLevel(4); setPosition(ll); },
      () => toast('현재 위치를 가져오지 못했어요. 위치 권한을 확인하거나 지도를 직접 눌러 주세요.', 'error'),
      { timeout: 10000, enableHighAccuracy: true });
  });

  const results = $('place-results');
  function searchPlace() {
    const q = $('place-q').value.trim();
    if (!q) return;
    if (!places) return toast('지도를 불러오지 못해 장소를 찾을 수 없어요.', 'error');
    places.keywordSearch(q, (data, status) => {
      results.hidden = false;
      if (status !== kakao.maps.services.Status.OK) {
        results.innerHTML = '<li style="padding:10px 12px;color:var(--ink-2)">찾는 장소가 없어요. 동네 이름으로 다시 찾아보세요.</li>';
        return;
      }
      results.innerHTML = data.slice(0, 8).map((p, i) =>
        `<li><button type="button" data-i="${i}">${escapeHtml(p.place_name)}<small>${escapeHtml(p.road_address_name || p.address_name)}</small></button></li>`).join('');
      results.querySelectorAll('button').forEach((b) => b.addEventListener('click', () => {
        const p = data[Number(b.dataset.i)];
        const ll = new kakao.maps.LatLng(Number(p.y), Number(p.x));
        map.setCenter(ll); map.setLevel(3);
        setPosition(ll);
        if (!$('landmark').value) $('landmark').value = p.place_name;
        results.hidden = true;
      }));
    });
  }
  $('place-search').addEventListener('click', searchPlace);
  $('place-q').addEventListener('keydown', (e) => { if (e.key === 'Enter') { e.preventDefault(); searchPlace(); } });

  // ---------- 사진 ----------
  const input = $('images');
  const thumbs = $('thumbs');
  const dropzone = $('dropzone');
  let files = [];

  function renderThumbs() {
    if (!files.length) return; // 고친 글은 새 사진을 고르기 전까지 지금 사진을 보여 준다
    thumbs.querySelectorAll('[data-existing], [data-new]').forEach((el) => el.remove());
    files.forEach((file, i) => {
      const fig = document.createElement('figure');
      fig.dataset.new = '';
      const url = URL.createObjectURL(file);
      fig.innerHTML = `<img src="${url}" alt="">${i === 0 ? '<figcaption>대표</figcaption>' : ''}<button type="button" aria-label="${i + 1}번째 사진 빼기">×</button>`;
      fig.querySelector('img').onload = () => URL.revokeObjectURL(url);
      fig.querySelector('button').addEventListener('click', () => {
        files.splice(i, 1);
        if (!files.length) thumbs.querySelectorAll('[data-new]').forEach((el) => el.remove());
        renderThumbs();
      });
      thumbs.append(fig);
    });
  }

  function addFiles(list) {
    clearError('images');
    for (const file of list) {
      if (!/^image\/(jpeg|png)$/.test(file.type)) { showError('images', 'JPG, PNG 사진만 올릴 수 있어요.'); continue; }
      if (file.size > 10 * 1024 * 1024) { showError('images', `${file.name} 은 10MB 가 넘어요.`); continue; }
      if (files.length >= maxImages) { showError('images', `사진은 ${maxImages}장까지 올릴 수 있어요.`); break; }
      files.push(file);
    }
    renderThumbs();
  }
  input.addEventListener('change', () => { addFiles(input.files); input.value = ''; });
  ['dragenter', 'dragover'].forEach((t) => dropzone.addEventListener(t, (e) => { e.preventDefault(); dropzone.classList.add('is-over'); }));
  ['dragleave', 'drop'].forEach((t) => dropzone.addEventListener(t, () => dropzone.classList.remove('is-over')));
  dropzone.addEventListener('drop', (e) => { e.preventDefault(); addFiles(e.dataTransfer.files); });

  // ---------- 오류 표시 ----------
  function errorEl(field) {
    let el = form.querySelector(`[data-error-for="${field}"]`);
    if (!el) {
      const target = $(field) || form.querySelector(`[name="${field}"]`);
      const holder = target?.closest('.field');
      if (!holder) return null;
      el = document.createElement('p');
      el.className = 'error';
      el.dataset.errorFor = field;
      holder.append(el);
    }
    return el;
  }
  function showError(field, message) {
    const el = errorEl(field);
    if (el) { el.textContent = message; el.hidden = false; }
    $(field)?.setAttribute('aria-invalid', 'true');
  }
  function clearError(field) {
    const el = form.querySelector(`[data-error-for="${field}"]`);
    if (el) el.hidden = true;
    $(field)?.removeAttribute('aria-invalid');
  }
  form.addEventListener('input', (e) => e.target.id && clearError(e.target.id));

  // ---------- 저장 ----------
  const value = (id) => $(id).value.trim();
  const radio = (name) => form.querySelector(`input[name="${name}"]:checked`)?.value;

  function collect() {
    return {
      kind: radio('kind'),
      title: value('title'),
      content: {
        reporterName: value('reporterName'),
        contact: value('contact'),
        lostDate: value('lostDate'),
        landmark: value('landmark') || null,
        address: value('address') || null,
        breed: value('breed'),
        pname: value('pname') || null,
        color: value('color'),
        gender: radio('gender') || '모름',
        age: value('age') === '' ? null : Number(value('age')),
        features: value('features') || null,
        hasMicrochip: radio('hasMicrochip') === 'true',
        latitude: Number(value('latitude')) || 0,
        longitude: Number(value('longitude')) || 0,
      },
    };
  }

  function validate(data) {
    const errors = {};
    if (!data.kind) errors.kind = '찾습니다·보호하고 있어요 중 하나를 골라 주세요';
    if (!data.title) errors.title = '제목을 입력해 주세요';
    if (!data.content.lostDate) errors.lostDate = '일시를 입력해 주세요';
    if (!data.content.latitude) errors.latitude = '지도를 눌러 위치를 골라 주세요';
    if (!data.content.breed) errors.breed = '품종을 입력해 주세요. 모르면 믹스견이라고 적어 주세요';
    if (!data.content.color) errors.color = '색상을 입력해 주세요';
    if (mode === 'create' && !files.length) errors.images = '사진을 한 장 이상 올려 주세요';
    if (!data.content.reporterName) errors.reporterName = '이름을 입력해 주세요';
    if (!/^0\d{1,2}-?\d{3,4}-?\d{4}$/.test(data.content.contact)) errors.contact = '전화번호를 010-1234-5678 형식으로 입력해 주세요';
    return errors;
  }

  function showErrors(errors, summary) {
    Object.entries(errors).forEach(([field, message]) => showError(field, message));
    const alert = $('form-alert');
    alert.textContent = summary || `입력하지 않았거나 잘못된 칸이 ${Object.keys(errors).length}개 있어요.`;
    alert.hidden = false;
    alert.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    $('form-alert').hidden = true;
    const data = collect();
    const errors = validate(data);
    if (Object.keys(errors).length) return showErrors(errors);

    if (mode === 'edit') data.imageChanged = files.length > 0;
    const body = new FormData();
    body.append('post', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    files.forEach((f) => body.append('images', f));

    const button = $('submit');
    const label = button.textContent;
    button.disabled = true;
    button.textContent = '저장하는 중…';
    try {
      const url = mode === 'edit' ? `/api/v1/posts/${form.dataset.id}` : '/api/v1/posts';
      const result = await api(url, { method: mode === 'edit' ? 'PUT' : 'POST', body });
      location.href = `/posts/${result.id}`;
    } catch (err) {
      showErrors(err.fields || {}, err.message);
      button.disabled = false;
      button.textContent = label;
    }
  });
})();
