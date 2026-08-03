import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';

const DataTableContext = createContext(null);

function useDataTable() {
  const context = useContext(DataTableContext);

  if (!context) {
    throw new Error(
      'DataTable sub-components must be used inside <DataTable>.'
    );
  }

  return context;
}

function compareValues(left, right) {
  if (left == null && right == null) {
    return 0;
  }

  if (left == null) {
    return -1;
  }

  if (right == null) {
    return 1;
  }

  if (typeof left === 'number' && typeof right === 'number') {
    return left - right;
  }

  return String(left).localeCompare(String(right), undefined, {
    numeric: true,
    sensitivity: 'base',
  });
}

export default function DataTable({
  data,
  pageSize = 10,
  children,
}) {
  const safeData = useMemo(
    () => Array.isArray(data) ? data : [],
    [data],
  );

  const [page, setPage] = useState(0);
  const [sortKey, setSortKey] = useState(null);
  const [sortDir, setSortDir] = useState('asc');

  const sortedRows = useMemo(() => {
    if (!sortKey) {
      return [...safeData];
    }

    return [...safeData].sort((left, right) => {
      const comparison = compareValues(
        left?.[sortKey],
        right?.[sortKey]
      );

      return sortDir === 'asc' ? comparison : -comparison;
    });
  }, [safeData, sortKey, sortDir]);

  const totalRows = sortedRows.length;
  const totalPages = Math.max(
    1,
    Math.ceil(totalRows / pageSize)
  );

  useEffect(() => {
    setPage((currentPage) =>
      Math.min(currentPage, totalPages - 1)
    );
  }, [totalPages]);

  useEffect(() => {
    setPage(0);
  }, [sortKey, sortDir]);

  const rows = useMemo(() => {
    const start = page * pageSize;

    return sortedRows.slice(
      start,
      start + pageSize
    );
  }, [sortedRows, page, pageSize]);

  const value = useMemo(
    () => ({
      rows,
      page,
      pageSize,
      totalRows,
      totalPages,
      sortKey,
      sortDir,
      setPage,
      setSortKey,
      setSortDir,
    }),
    [
      rows,
      page,
      pageSize,
      totalRows,
      totalPages,
      sortKey,
      sortDir,
    ]
  );

  return (
    <DataTableContext.Provider value={value}>
      <div className="data-table">
        {children}
      </div>
    </DataTableContext.Provider>
  );
}

function Header({ columns }) {
  const {
    sortKey,
    sortDir,
    setSortKey,
    setSortDir,
  } = useDataTable();

  function handleSort(column) {
    if (column.sortable === false) {
      return;
    }

    if (sortKey === column.key) {
      setSortDir((current) =>
        current === 'asc' ? 'desc' : 'asc'
      );

      return;
    }

    setSortKey(column.key);
    setSortDir('asc');
  }

  return (
    <div className="data-table__header" role="row">
      {columns.map((column) => {
        const active = sortKey === column.key;

        const ariaSort = active
          ? sortDir === 'asc'
            ? 'ascending'
            : 'descending'
          : 'none';

        return (
          <button
            key={column.key}
            type="button"
            role="columnheader"
            aria-sort={ariaSort}
            className={
              active
                ? 'data-table__th data-table__th--active'
                : 'data-table__th'
            }
            disabled={column.sortable === false}
            onClick={() => handleSort(column)}
          >
            {column.label}

            {active && (
              <span aria-hidden="true">
                {sortDir === 'asc' ? ' ▲' : ' ▼'}
              </span>
            )}
          </button>
        );
      })}
    </div>
  );
}

function Body({ renderRow }) {
  const { rows } = useDataTable();

  return (
    <div className="data-table__body" role="rowgroup">
      {rows.map((row, index) => (
        <div
          key={row.id ?? row.tradeRef ?? index}
          className="data-table__row"
          role="row"
        >
          {renderRow(row, index)}
        </div>
      ))}

      {rows.length === 0 && (
        <div className="data-table__empty">
          No records found.
        </div>
      )}
    </div>
  );
}

function Pagination() {
  const {
    page,
    totalPages,
    totalRows,
    setPage,
  } = useDataTable();

  return (
    <nav
      className="data-table__pagination"
      aria-label="Table pagination"
    >
      <button
        type="button"
        disabled={page === 0}
        onClick={() =>
          setPage((current) => current - 1)
        }
      >
        Previous
      </button>

      <span>
        Page {page + 1} of {totalPages} · {totalRows} rows
      </span>

      <button
        type="button"
        disabled={page >= totalPages - 1}
        onClick={() =>
          setPage((current) => current + 1)
        }
      >
        Next
      </button>
    </nav>
  );
}

DataTable.Header = Header;
DataTable.Body = Body;
DataTable.Pagination = Pagination;
