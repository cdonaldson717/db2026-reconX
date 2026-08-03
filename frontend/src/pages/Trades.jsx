// TICKET-ADV114 — Compound DataTable.
// TICKET-ADV117 — useDebouncedSearch.
import React, { useCallback, useEffect, useState } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import DataTable from '@components/DataTable.jsx';
import { TradeRow } from '@components/TradeRow.jsx';
import { useDebouncedSearch } from '@hooks/useDebouncedSearch.js';
import { api } from '@services/apiService.js';

function Trades() {
  const [search, setSearch] = useState('');
  const debounced = useDebouncedSearch(search, 300);
  const [selectedId, setSelectedId] = useState(null);
  const [trades, setTrades] = useState([]);

  const handleSelect = useCallback((id) => {
    setSelectedId(id);
  }, []);

  useEffect(() => {
    let active = true;
    const query = new URLSearchParams({ size: '100' });
    if (debounced) query.set('status', debounced);

    api.listTrades(`?${query.toString()}`)
      .then((response) => {
        if (active) setTrades(response.content ?? response.items ?? []);
      })
      .catch(() => {
        if (active) setTrades([]);
      });

    return () => {
      active = false;
    };
  }, [debounced]);

  return (
    <section>
      <h2>Trades</h2>
      <input
        aria-label="Filter by status"
        placeholder="status filter (PENDING/MATCHED/…)"
        value={search}
        onChange={(e) => setSearch(e.target.value.toUpperCase())}
      />
      <DataTable data={trades} pageSize={20}>
        <DataTable.Header columns={[
          { key: 'tradeRef', label: 'Ref' },
          { key: 'symbol',   label: 'Symbol' },
          { key: 'qty',      label: 'Qty' },
          { key: 'price',    label: 'Price' },
          { key: 'status',   label: 'Status' },
        ]} />
        <DataTable.Body
          renderRow={(trade) => (
            <TradeRow
              key={trade.id ?? trade.tradeRef}
              trade={trade}
              onClick={handleSelect}
              isSelected={(trade.id ?? trade.tradeRef) === selectedId}
            />
          )}
        />
        <DataTable.Pagination />
      </DataTable>
    </section>
  );
}

export default withAuth(Trades);
