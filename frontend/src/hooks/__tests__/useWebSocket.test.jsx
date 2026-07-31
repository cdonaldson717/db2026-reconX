import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useWebSocket } from '../useWebSocket.js';

class FakeWebSocket {
  static CONNECTING = 0;
  static OPEN = 1;
  static CLOSED = 3;
  static instances = [];

  constructor(url) {
    this.url = url;
    this.readyState = FakeWebSocket.CONNECTING;
    this.send = vi.fn();
    FakeWebSocket.instances.push(this);
  }

  open() {
    this.readyState = FakeWebSocket.OPEN;
    this.onopen?.();
  }

  message(data) {
    this.onmessage?.({ data });
  }

  close() {
    this.readyState = FakeWebSocket.CLOSED;
    this.onclose?.();
  }
}

describe('useWebSocket', () => {
  beforeEach(() => {
    FakeWebSocket.instances = [];
    vi.stubGlobal('WebSocket', FakeWebSocket);
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  it('opens one socket, parses messages, sends only while open, and closes on unmount', () => {
    const { result, unmount } = renderHook(() => useWebSocket('ws://localhost/stream'));
    const socket = FakeWebSocket.instances[0];

    expect(FakeWebSocket.instances).toHaveLength(1);
    expect(result.current.status).toBe('connecting');

    act(() => result.current.send('too early'));
    expect(socket.send).not.toHaveBeenCalled();

    act(() => socket.open());
    expect(result.current.status).toBe('open');

    act(() => socket.message('{"tradeRef":"TR-1"}'));
    expect(result.current.data).toEqual({ tradeRef: 'TR-1' });

    act(() => result.current.send({ action: 'subscribe' }));
    expect(socket.send).toHaveBeenCalledWith('{"action":"subscribe"}');

    act(() => socket.message('not-json'));
    expect(result.current.data).toBe('not-json');

    unmount();
    expect(socket.readyState).toBe(FakeWebSocket.CLOSED);
    expect(FakeWebSocket.instances).toHaveLength(1);
  });

  it('reconnects with capped exponential backoff and stops at maxRetries', () => {
    vi.useFakeTimers();
    const { result } = renderHook(() => useWebSocket('ws://localhost/stream', {
      maxRetries: 3,
      baseDelay: 100,
      maxDelay: 250,
    }));

    act(() => FakeWebSocket.instances[0].close());
    expect(result.current.status).toBe('closed');

    act(() => vi.advanceTimersByTime(99));
    expect(FakeWebSocket.instances).toHaveLength(1);
    act(() => vi.advanceTimersByTime(1));
    expect(FakeWebSocket.instances).toHaveLength(2);

    act(() => FakeWebSocket.instances[1].close());
    act(() => vi.advanceTimersByTime(200));
    expect(FakeWebSocket.instances).toHaveLength(3);

    act(() => FakeWebSocket.instances[2].close());
    act(() => vi.advanceTimersByTime(250));
    expect(FakeWebSocket.instances).toHaveLength(4);

    act(() => FakeWebSocket.instances[3].close());
    act(() => vi.runAllTimers());
    expect(FakeWebSocket.instances).toHaveLength(4);
  });

  it('cancels a pending reconnect when unmounted', () => {
    vi.useFakeTimers();
    const { unmount } = renderHook(() => useWebSocket('ws://localhost/stream'));

    act(() => FakeWebSocket.instances[0].close());
    unmount();
    act(() => vi.runAllTimers());

    expect(FakeWebSocket.instances).toHaveLength(1);
  });
});
