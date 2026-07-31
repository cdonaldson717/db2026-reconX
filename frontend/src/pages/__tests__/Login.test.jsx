import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import Login from '../Login.jsx';
import { api } from '../../services/apiService.js';

const login = vi.fn();
const navigate = vi.fn();

vi.mock('../../context/AuthContext.jsx', () => ({
  useAuth: () => ({ login }),
}));

vi.mock('react-router-dom', async (importOriginal) => {
  const original = await importOriginal();
  return { ...original, useNavigate: () => navigate };
});

describe('<Login>', () => {
  beforeEach(() => {
    login.mockReset();
    navigate.mockReset();
    vi.restoreAllMocks();
  });

  it('exchanges credentials, stores authentication, and navigates home', async () => {
    vi.spyOn(api, 'login').mockResolvedValue({ token: 'jwt-token', role: 'ADMIN' });
    render(<Login />, { wrapper: MemoryRouter });

    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }));

    await waitFor(() => {
      expect(api.login).toHaveBeenCalledWith('admin@db.com', 'admin123');
      expect(login).toHaveBeenCalledWith('jwt-token', 'ADMIN');
      expect(navigate).toHaveBeenCalledWith('/');
    });
  });

  it('shows the API error and does not authenticate on failure', async () => {
    vi.spyOn(api, 'login').mockRejectedValue(new Error('HTTP 401: Invalid credentials'));
    render(<Login />, { wrapper: MemoryRouter });

    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('HTTP 401: Invalid credentials');
    expect(login).not.toHaveBeenCalled();
    expect(navigate).not.toHaveBeenCalled();
  });
});
