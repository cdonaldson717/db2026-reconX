// TICKET-ADV104 — browser-managed Server-Sent Events subscription.
(function () {
  const STREAM_URL = document.body?.dataset.streamUrl || '/api/v1/trades/stream';
  const FEED_EL = document.getElementById('trade-feed');
  const STATUS_EL = document.getElementById('sse-status');

  if (!FEED_EL || !STATUS_EL) return;

  let sse = null;
  let connectionStatus = 'connecting';

  function updateConnectionBadge(text, variant) {
    connectionStatus = variant;
    STATUS_EL.textContent = text;
    STATUS_EL.className = `sse-status sse-status--${connectionStatus}`;
  }

  // TICKET-ADV105 replaces this basic renderer with the bounded, escaped row.
  function prependTradeRow(trade) {
    const row = document.createElement('article');
    row.className = `trade-card trade-card--${String(trade.status).toLowerCase()}`;
    row.textContent = [
      trade.tradeRef,
      trade.symbol,
      `qty=${trade.qty}`,
      `price=${trade.price}`,
      `[${trade.status}]`,
    ].join(' ');
    FEED_EL.prepend(row);
  }

  function connect() {
    updateConnectionBadge('Connecting…', 'connecting');
    sse = new EventSource(STREAM_URL);

    sse.onopen = function () {
      updateConnectionBadge('Live', 'live');
    };

    sse.onmessage = function (event) {
      try {
        prependTradeRow(JSON.parse(event.data));
      } catch (error) {
        console.error('Ignoring malformed trade event', error);
      }
    };

    sse.onerror = function () {
      // EventSource reconnects automatically; do not create another instance.
      updateConnectionBadge('Reconnecting…', 'reconnecting');
    };
  }

  window.addEventListener('beforeunload', function () {
    sse?.close();
  });

  connect();
})();
