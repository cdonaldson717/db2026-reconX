// TICKET-ADV125 — RTL test against the DataTable compound component.
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect } from 'vitest';
import DataTable from '../DataTable/DataTable.jsx';

describe('<DataTable>', () => {
  it('renders columns and rows', () => {
    render(
      <DataTable data={[{ id: 1 }, { id: 2 }]}>
        <DataTable.Header columns={[{ key: 'a', label: 'Alpha' }, { key: 'b', label: 'Beta' }]} />
        <DataTable.Body renderRow={(r) => <span>row {r.id}</span>} />
      </DataTable>
    );

    expect(screen.getByRole('columnheader', { name: 'Alpha' })).toBeInTheDocument();
    expect(screen.getByRole('columnheader', { name: 'Beta' })).toBeInTheDocument();
    expect(screen.getByText('row 1')).toBeInTheDocument();
    expect(screen.getByText('row 2')).toBeInTheDocument();
  });

  it('sorts by a header when it is clicked', async () => {
    render(
      <DataTable data={[{ id: 1, a: 'Zulu' }, { id: 2, a: 'Alpha' }]}>
        <DataTable.Header columns={[{ key: 'a', label: 'Alpha' }]} />
        <DataTable.Body renderRow={(row) => <span>{row.a}</span>} />
      </DataTable>
    );
    await userEvent.click(screen.getByRole('columnheader', { name: 'Alpha' }));

    expect(screen.getByRole('columnheader', { name: 'Alpha' })).toHaveAttribute(
      'aria-sort',
      'ascending',
    );
    expect(screen.getAllByRole('row')[1]).toHaveTextContent('Alpha');
  });
});
