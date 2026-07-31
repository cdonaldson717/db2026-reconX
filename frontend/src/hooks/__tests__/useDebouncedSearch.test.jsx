import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useDebouncedSearch } from '../useDebouncedSearch.js';

describe('useDebouncedSearch', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  it('returns the initial query immediately', () => {
    const { result } = renderHook(() => useDebouncedSearch('initial'));
    expect(result.current).toBe('initial');
  });

  it('publishes only the latest query after the trailing delay', () => {
    const { result, rerender } = renderHook(
      ({ query }) => useDebouncedSearch(query, 300),
      { initialProps: { query: '' } },
    );

    rerender({ query: 'A' });
    act(() => vi.advanceTimersByTime(100));
    rerender({ query: 'AA' });
    act(() => vi.advanceTimersByTime(100));
    rerender({ query: 'AAPL' });

    act(() => vi.advanceTimersByTime(299));
    expect(result.current).toBe('');

    act(() => vi.advanceTimersByTime(1));
    expect(result.current).toBe('AAPL');
  });

  it('uses a changed delay and cancels the previous timer', () => {
    const { result, rerender, unmount } = renderHook(
      ({ query, delay }) => useDebouncedSearch(query, delay),
      { initialProps: { query: 'old', delay: 500 } },
    );

    rerender({ query: 'new', delay: 100 });
    act(() => vi.advanceTimersByTime(99));
    expect(result.current).toBe('old');

    act(() => vi.advanceTimersByTime(1));
    expect(result.current).toBe('new');

    unmount();
    expect(vi.getTimerCount()).toBe(0);
  });
});
