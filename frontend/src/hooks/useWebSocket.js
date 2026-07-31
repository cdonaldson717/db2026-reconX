// TICKET-ADV115 — useWebSocket(url) with capped exponential reconnect backoff.
import { useCallback, useEffect, useRef, useState } from 'react';

const DEFAULT_BASE_DELAY = 500;
const DEFAULT_MAX_DELAY = 30_000;

export function useWebSocket(
  url,
  {
    reconnect = true,
    maxRetries = 5,
    baseDelay = DEFAULT_BASE_DELAY,
    maxDelay = DEFAULT_MAX_DELAY,
  } = {},
) {
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('connecting');
  const wsRef = useRef(null);
  const retriesRef = useRef(0);
  const timerRef = useRef(null);
  const shouldStopRef = useRef(false);

  const connect = useCallback(() => {
    if (shouldStopRef.current || !url) return;

    setStatus('connecting');
    const socket = new WebSocket(url);
    wsRef.current = socket;

    socket.onopen = () => {
      if (shouldStopRef.current || wsRef.current !== socket) return;
      retriesRef.current = 0;
      setStatus('open');
    };

    socket.onmessage = (event) => {
      if (shouldStopRef.current || wsRef.current !== socket) return;
      try {
        setData(JSON.parse(event.data));
      } catch {
        setData(event.data);
      }
    };

    socket.onerror = () => {
      if (!shouldStopRef.current && wsRef.current === socket) {
        setStatus('error');
      }
    };

    socket.onclose = () => {
      if (shouldStopRef.current || wsRef.current !== socket) return;

      wsRef.current = null;
      setStatus('closed');
      if (!reconnect || retriesRef.current >= maxRetries) return;

      const delay = Math.min(
        maxDelay,
        baseDelay * 2 ** retriesRef.current,
      );
      retriesRef.current += 1;
      timerRef.current = setTimeout(() => {
        timerRef.current = null;
        connect();
      }, delay);
    };
  }, [baseDelay, maxDelay, maxRetries, reconnect, url]);

  useEffect(() => {
    shouldStopRef.current = false;
    retriesRef.current = 0;
    connect();

    return () => {
      shouldStopRef.current = true;
      if (timerRef.current !== null) {
        clearTimeout(timerRef.current);
        timerRef.current = null;
      }

      const socket = wsRef.current;
      wsRef.current = null;
      if (socket && socket.readyState <= WebSocket.OPEN) socket.close();
    };
  }, [connect]);

  const send = useCallback((payload) => {
    const socket = wsRef.current;
    if (socket?.readyState !== WebSocket.OPEN) return;
    socket.send(typeof payload === 'string' ? payload : JSON.stringify(payload));
  }, []);

  return { data, status, send };
}
