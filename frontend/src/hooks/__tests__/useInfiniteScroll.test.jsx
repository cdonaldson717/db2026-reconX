import { render } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useInfiniteScroll } from '../useInfiniteScroll.js';

class FakeIntersectionObserver {
  static instances = [];

  constructor(callback, options) {
    this.callback = callback;
    this.options = options;
    this.observe = vi.fn();
    this.disconnect = vi.fn();
    FakeIntersectionObserver.instances.push(this);
  }

  trigger(isIntersecting) {
    this.callback([{ isIntersecting }]);
  }
}

function Harness({ loadMore, rootMargin }) {
  const sentinelRef = useInfiniteScroll(loadMore, { rootMargin });
  return <div data-testid="sentinel" ref={sentinelRef} />;
}

describe('useInfiniteScroll', () => {
  beforeEach(() => {
    FakeIntersectionObserver.instances = [];
    vi.stubGlobal('IntersectionObserver', FakeIntersectionObserver);
  });

  afterEach(() => vi.unstubAllGlobals());

  it('observes the sentinel and loads only when it intersects', () => {
    const loadMore = vi.fn();
    const { getByTestId } = render(<Harness loadMore={loadMore} rootMargin="150px" />);
    const observer = FakeIntersectionObserver.instances[0];

    expect(observer.options).toEqual({ rootMargin: '150px' });
    expect(observer.observe).toHaveBeenCalledOnce();
    expect(observer.observe).toHaveBeenCalledWith(getByTestId('sentinel'));

    observer.trigger(false);
    expect(loadMore).not.toHaveBeenCalled();

    observer.trigger(true);
    expect(loadMore).toHaveBeenCalledOnce();
  });

  it('uses the latest callback without recreating the observer', () => {
    const firstLoadMore = vi.fn();
    const secondLoadMore = vi.fn();
    const { rerender } = render(<Harness loadMore={firstLoadMore} />);
    const observer = FakeIntersectionObserver.instances[0];

    rerender(<Harness loadMore={secondLoadMore} />);
    observer.trigger(true);

    expect(FakeIntersectionObserver.instances).toHaveLength(1);
    expect(firstLoadMore).not.toHaveBeenCalled();
    expect(secondLoadMore).toHaveBeenCalledOnce();
  });

  it('recreates for option changes and disconnects during cleanup', () => {
    const loadMore = vi.fn();
    const { rerender, unmount } = render(
      <Harness loadMore={loadMore} rootMargin="100px" />,
    );
    const firstObserver = FakeIntersectionObserver.instances[0];

    rerender(<Harness loadMore={loadMore} rootMargin="300px" />);
    const secondObserver = FakeIntersectionObserver.instances[1];

    expect(firstObserver.disconnect).toHaveBeenCalledOnce();
    expect(secondObserver.options).toEqual({ rootMargin: '300px' });

    unmount();
    expect(secondObserver.disconnect).toHaveBeenCalledOnce();
  });
});
