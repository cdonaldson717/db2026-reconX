import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useTradeStream } from '../useTradeStream.js';

class FakeEventSource {
  static instances = [];

  constructor(url) {
    this.url = url;
    this.close = vi.fn();
    FakeEventSource.instances.push(this);
  }

  open() {
    this.onopen?.();
  }

  error() {
    this.onerror?.();
  }

  message(data) {
    this.onmessage?.({ data });
  }
}

describe('useTradeStream', () => {
  beforeEach(() => {
    FakeEventSource.instances = [];
    vi.stubGlobal('EventSource', FakeEventSource);
  });

  it('opens the default stream, tracks connectivity, and closes on unmount', () => {
    const { result, unmount } = renderHook(() => useTradeStream());
    const source = FakeEventSource.instances[0];

    expect(FakeEventSource.instances).toHaveLength(1);
    expect(source.url).toBe('/api/v1/trades/stream');
    expect(result.current.isConnected).toBe(false);

    act(() => source.open());
    expect(result.current.isConnected).toBe(true);

    act(() => source.error());
    expect(result.current.isConnected).toBe(false);

    unmount();
    expect(source.close).toHaveBeenCalledOnce();
  });

  it('prepends parsed trades and ignores malformed messages', () => {
    const { result } = renderHook(() => useTradeStream('/stream/trades'));
    const source = FakeEventSource.instances[0];

    act(() => {
      source.message('{"id":1}');
      source.message('not-json');
      source.message('{"id":2}');
    });

    expect(result.current.trades).toEqual([{ id: 2 }, { id: 1 }]);
  });

  it('keeps only the 200 newest trades', () => {
    const { result } = renderHook(() => useTradeStream());
    const source = FakeEventSource.instances[0];

    act(() => {
      for (let id = 1; id <= 205; id += 1) {
        source.message(JSON.stringify({ id }));
      }
    });

    expect(result.current.trades).toHaveLength(200);
    expect(result.current.trades[0]).toEqual({ id: 205 });
    expect(result.current.trades[199]).toEqual({ id: 6 });
  });

  it('replaces and closes the stream when the URL changes', () => {
    const { rerender } = renderHook(
      ({ url }) => useTradeStream(url),
      { initialProps: { url: '/stream/one' } },
    );
    const firstSource = FakeEventSource.instances[0];

    rerender({ url: '/stream/two' });

    expect(firstSource.close).toHaveBeenCalledOnce();
    expect(FakeEventSource.instances).toHaveLength(2);
    expect(FakeEventSource.instances[1].url).toBe('/stream/two');
  });
});
