// TICKET-ADV116 — useTradeStream() — SSE subscription returning live trades.
import { useEffect, useState } from 'react';

const MAX_BUFFER_SIZE = 200;

export function useTradeStream(url = '/api/v1/trades/stream') {
  const [trades, setTrades] = useState([]);
  const [isConnected, setConnected] = useState(false);

  useEffect(() => {
    const source = new EventSource(url);

    source.onopen = () => setConnected(true);
    source.onerror = () => setConnected(false);
    source.onmessage = (event) => {
      try {
        const trade = JSON.parse(event.data);
        setTrades((previous) => (
          [trade, ...previous].slice(0, MAX_BUFFER_SIZE)
        ));
      } catch {
        // Ignore malformed events and keep the stream alive for later messages.
      }
    };

    return () => source.close();
  }, [url]);

  return { trades, isConnected };
}
