# Ticket ADV127 profiling check

The dashboard summary cards and authenticated dashboard component are memoized.
Their inputs are primitive summary values or a stable `trades` reference, so an
unrelated parent render no longer re-renders the dashboard subtree.

To capture comparable evidence:

1. Run `npm run dev` and open React DevTools.
2. Enable **Highlight updates when components render**.
3. In the Profiler, record navigation to the dashboard followed by a theme
   toggle. Save this trace as the baseline when testing the pre-fix commit.
4. Repeat the identical interaction on `ticket-127` and save the after trace.
5. Select `StatCard` in both flame charts. In the after trace it should not
   render when the theme changes and its `label` and `value` props are unchanged.

The cause was parent-driven rendering: `Dashboard` and each `StatCard` were
ordinary function components, so unrelated ancestor updates executed them even
when their inputs were unchanged. `React.memo` adds the required prop equality
bailout while hook state updates from the trade stream still render normally.
