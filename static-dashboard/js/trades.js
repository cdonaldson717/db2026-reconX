// TICKET-ADV106 - sort + resize + sticky-header trade table.
(function () {
  const table = document.getElementById('trades-table');
  const tbody = document.getElementById('trades-tbody');
  if (!table || !tbody) return;

  let rows = [];

  const fallbackRows = [
    { tradeRef: 'EQU-20260731-0001', instrumentSymbol: 'SAP.DE', quantity: 1000, price: 125.5, status: 'MATCHED' },
    { tradeRef: 'EQU-20260731-0002', instrumentSymbol: 'AAPL', quantity: 500, price: 178.2, status: 'BREAK' },
    { tradeRef: 'FX-20260731-0001', instrumentSymbol: 'EUR/USD', quantity: 1000000, price: 1.0852, status: 'PENDING' },
  ];

  function renderRows() {
    tbody.innerHTML = rows.map((row) => `
      <tr>
        <td>${row.tradeRef ?? ''}</td>
        <td>${row.instrumentSymbol ?? row.symbol ?? ''}</td>
        <td>${row.quantity ?? ''}</td>
        <td>${row.price ?? ''}</td>
        <td>${row.status ?? ''}</td>
      </tr>
    `).join('');
  }

  table.querySelectorAll('thead th').forEach((th) => {
    th.addEventListener('click', (event) => {
      if (event.target.classList.contains('resize-handle')) return;

      const col = th.dataset.col;
      const type = th.dataset.type || 'string';
      const dir = th.getAttribute('aria-sort') === 'ascending' ? 'descending' : 'ascending';
      const mult = dir === 'ascending' ? 1 : -1;

      table.querySelectorAll('thead th').forEach((other) => other.removeAttribute('aria-sort'));
      th.setAttribute('aria-sort', dir);

      rows.sort((a, b) => {
        const av = a[col];
        const bv = b[col];
        if (type === 'number') return (Number(av) - Number(bv)) * mult;
        return String(av ?? '').localeCompare(String(bv ?? '')) * mult;
      });

      renderRows();
    });
  });

  table.querySelectorAll('.resize-handle').forEach((handle) => {
    handle.addEventListener('mousedown', (event) => {
      event.preventDefault();

      const th = handle.closest('th');
      const startX = event.clientX;
      const startWidth = th.offsetWidth;

      function onMove(moveEvent) {
        th.style.width = `${startWidth + moveEvent.clientX - startX}px`;
      }

      function onUp() {
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onUp);
      }

      document.addEventListener('mousemove', onMove);
      document.addEventListener('mouseup', onUp);
    });
  });

  fetch('http://localhost:8080/api/v1/trades?size=200')
    .then((response) => response.ok ? response.json() : Promise.reject(new Error('failed to load trades')))
    .then((data) => {
      rows = data.content || data;
      renderRows();
    })
    .catch(() => {
      rows = fallbackRows;
      renderRows();
    });
})();
