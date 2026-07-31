// TICKET-ADV122 - Lazy + Suspense for route-based code splitting.
import React, { Suspense, lazy } from 'react';
import { Routes, Route, Link, Navigate } from 'react-router-dom';
import { withErrorBoundary } from '@components/withErrorBoundary.jsx';
import { useTheme } from '@context/ThemeContext.jsx';

const Dashboard = lazy(() => import('@pages/Dashboard.jsx'));
const Trades = lazy(() => import('@pages/Trades.jsx'));
const AddTrade = lazy(() => import('@pages/AddTrade.jsx'));
const Login = lazy(() => import('@pages/Login.jsx'));

function PageSkeleton() {
  return (
    <section aria-label="Loading page" className="page-skeleton">
      <div className="page-skeleton__title" />
      <div className="page-skeleton__line" />
      <div className="page-skeleton__line page-skeleton__line--short" />
      <div className="page-skeleton__card-row">
        <div className="page-skeleton__card" />
        <div className="page-skeleton__card" />
        <div className="page-skeleton__card" />
      </div>
    </section>
  );
}

function App() {
  const { theme, toggle } = useTheme();

  return (
    <div className="layout">
      <header className="layout__header">
        <h1>ReconX</h1>
        <nav className="layout__nav">
          <Link to="/">Dashboard</Link>
          <Link to="/trades">Trades</Link>
          <Link to="/trades/new">Add trade</Link>
        </nav>
        <button
          type="button"
          onClick={toggle}
          aria-label={`Switch to ${theme === 'light' ? 'dark' : 'light'} theme`}
          aria-pressed={theme === 'dark'}
        >
          {theme === 'light' ? 'Dark' : 'Light'} mode
        </button>
      </header>
      <main className="layout__main">
        <Suspense fallback={<PageSkeleton />}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<Dashboard />} />
            <Route path="/trades" element={<Trades />} />
            <Route path="/trades/new" element={<AddTrade />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </main>
    </div>
  );
}

export default withErrorBoundary(App);
