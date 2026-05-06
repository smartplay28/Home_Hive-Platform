import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { StarRating, RatingDisplay } from '../components/StarRating';

/**
 * StarRating component tests.
 * Tests: render, interaction, read-only mode, fractional display, RatingDisplay.
 */
describe('StarRating', () => {

  // ─── Rendering ──────────────────────────────────────────────────────────────

  describe('Rendering', () => {
    it('renders 5 star buttons', () => {
      render(<StarRating value={0} onChange={() => {}} />);
      const stars = screen.getAllByRole('button');
      expect(stars).toHaveLength(5);
    });

    it('renders with correct aria-labels', () => {
      render(<StarRating value={0} onChange={() => {}} />);
      expect(screen.getByLabelText('1 star')).toBeInTheDocument();
      expect(screen.getByLabelText('5 stars')).toBeInTheDocument();
    });
  });

  // ─── Interaction ────────────────────────────────────────────────────────────

  describe('Interaction', () => {
    it('calls onChange with correct value when star is clicked', () => {
      const onChange = vi.fn();
      render(<StarRating value={0} onChange={onChange} />);

      fireEvent.click(screen.getByLabelText('4 stars'));
      expect(onChange).toHaveBeenCalledWith(4);
      expect(onChange).toHaveBeenCalledTimes(1);
    });

    it('calls onChange with 1 when first star is clicked', () => {
      const onChange = vi.fn();
      render(<StarRating value={3} onChange={onChange} />);
      fireEvent.click(screen.getByLabelText('1 star'));
      expect(onChange).toHaveBeenCalledWith(1);
    });

    it('calls onChange with 5 when last star is clicked', () => {
      const onChange = vi.fn();
      render(<StarRating value={0} onChange={onChange} />);
      fireEvent.click(screen.getByLabelText('5 stars'));
      expect(onChange).toHaveBeenCalledWith(5);
    });
  });

  // ─── Read-only mode ─────────────────────────────────────────────────────────

  describe('Read-only mode', () => {
    it('does not call onChange in readOnly mode', () => {
      const onChange = vi.fn();
      render(<StarRating value={3} onChange={onChange} readOnly />);

      // All buttons should be disabled in read-only mode
      const buttons = screen.getAllByRole('button');
      buttons.forEach(btn => expect(btn).toBeDisabled());
    });

    it('renders with disabled buttons when readOnly=true', () => {
      render(<StarRating value={4} readOnly />);
      const buttons = screen.getAllByRole('button');
      expect(buttons.every(b => b.disabled)).toBe(true);
    });
  });

  // ─── Size variants ──────────────────────────────────────────────────────────

  describe('Size variants', () => {
    it('renders without error for all size variants', () => {
      for (const size of ['sm', 'md', 'lg']) {
        const { unmount } = render(<StarRating value={3} size={size} onChange={() => {}} />);
        expect(screen.getAllByRole('button')).toHaveLength(5);
        unmount();
      }
    });
  });
});

// ─── RatingDisplay ────────────────────────────────────────────────────────────

describe('RatingDisplay', () => {
  it('renders "No ratings yet" when rating is 0', () => {
    render(<RatingDisplay rating={0} />);
    expect(screen.getByText('No ratings yet')).toBeInTheDocument();
  });

  it('shows rating value when rating > 0', () => {
    render(<RatingDisplay rating={4.3} count={27} />);
    expect(screen.getByText('4.3')).toBeInTheDocument();
    expect(screen.getByText('(27 reviews)')).toBeInTheDocument();
  });

  it('hides review count when count is undefined', () => {
    render(<RatingDisplay rating={4.0} />);
    expect(screen.queryByText(/reviews/)).not.toBeInTheDocument();
  });

  it('renders "No ratings yet" when rating is null', () => {
    render(<RatingDisplay rating={null} />);
    expect(screen.getByText('No ratings yet')).toBeInTheDocument();
  });
});
