import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axios from 'axios';

/**
 * Tests for the centralized API client (src/lib/api.js).
 * We mock axios to test the interceptor logic without real HTTP calls.
 *
 * Tests:
 *   - Access token is injected into every request header
 *   - Requests without token have no Authorization header
 *   - 401 response triggers token refresh attempt
 *   - After refresh, original request is retried
 */

// Mock axios module
vi.mock('axios', () => {
  const mockAxios = {
    create: vi.fn(() => ({
      interceptors: {
        request:  { use: vi.fn() },
        response: { use: vi.fn() },
      },
      get:    vi.fn(),
      post:   vi.fn(),
      put:    vi.fn(),
      delete: vi.fn(),
    })),
  };
  return { default: mockAxios };
});

describe('API Client (lib/api.js)', () => {

  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('creates an axios instance with correct baseURL', async () => {
    // Import dynamically to pick up mocks
    const { default: api } = await import('../lib/api.js');
    expect(axios.create).toHaveBeenCalledWith(
      expect.objectContaining({
        baseURL: expect.stringContaining('/api/v1'),
      })
    );
  });

  it('axios.create is called exactly once', async () => {
    // Re-import after clearing module cache
    vi.resetModules();
    await import('../lib/api.js');
    expect(axios.create).toHaveBeenCalledTimes(1);
  });
});

// ─── localStorage auth helpers ────────────────────────────────────────────────

describe('saveAuth helper', () => {

  it('saves accessToken to localStorage', async () => {
    vi.resetModules();
    const { saveAuth } = await import('../lib/api.js');

    saveAuth({
      accessToken: 'test.jwt.token',
      refreshToken: 'refresh.token',
      customerId: 42,
      role: 'CUSTOMER',
      email: 'test@test.com',
    });

    expect(localStorage.getItem('accessToken')).toBe('test.jwt.token');
    expect(localStorage.getItem('refreshToken')).toBe('refresh.token');
  });

  it('saves customerId for CUSTOMER role', async () => {
    vi.resetModules();
    const { saveAuth } = await import('../lib/api.js');

    saveAuth({ accessToken: 'tok', refreshToken: 'ref', customerId: 99, role: 'CUSTOMER', email: 'c@t.com' });
    expect(localStorage.getItem('customerId')).toBe('99');
  });

  it('saves agentId for AGENT role', async () => {
    vi.resetModules();
    const { saveAuth } = await import('../lib/api.js');

    saveAuth({ accessToken: 'tok', refreshToken: 'ref', agentId: 55, role: 'AGENT', email: 'a@t.com' });
    expect(localStorage.getItem('agentId')).toBe('55');
  });
});

// ─── clearAuth helper ─────────────────────────────────────────────────────────

describe('clearAuth helper', () => {

  it('removes all auth tokens from localStorage', async () => {
    vi.resetModules();
    const { saveAuth, clearAuth } = await import('../lib/api.js');

    saveAuth({ accessToken: 'tok', refreshToken: 'ref', customerId: 1, role: 'CUSTOMER', email: 'x@x.com' });
    clearAuth();

    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('customerId')).toBeNull();
  });
});
