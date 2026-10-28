/* 관리자 버튼 (상세 화면과 신고 관리 화면에서 같이 쓴다) */
(function () {
  const { api, toast } = window.FMD;
  document.querySelectorAll('[data-admin-url]').forEach((button) => button.addEventListener('click', async () => {
    button.disabled = true;
    try {
      await api(button.dataset.adminUrl, { method: 'POST' });
      location.reload();
    } catch (e) {
      toast(e.message, 'error');
      button.disabled = false;
    }
  }));
  document.querySelectorAll('[data-admin-delete]').forEach((button) => button.addEventListener('click', async () => {
    if (!confirm('이 제보와 사진을 지울까요? 되돌릴 수 없어요.')) return;
    try {
      await api(button.dataset.adminDelete, { method: 'DELETE' });
      location.reload();
    } catch (e) {
      toast(e.message, 'error');
    }
  }));
})();
