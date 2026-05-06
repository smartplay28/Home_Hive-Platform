/**
 * StarRating — interactive 5-star rating component.
 *
 * Props:
 *   value (number)        — current selected rating (0-5)
 *   onChange (function)   — called with new rating when star is clicked
 *   readOnly (boolean)    — display-only mode (no click events)
 *   size (string)         — 'sm' | 'md' | 'lg'
 *
 * Usage:
 *   <StarRating value={rating} onChange={setRating} size="lg" />
 *   <StarRating value={4.3} readOnly size="sm" />
 */

import React, { useState } from 'react';

const SIZES = {
  sm: 'text-lg',
  md: 'text-2xl',
  lg: 'text-4xl',
};

export const StarRating = ({ value = 0, onChange, readOnly = false, size = 'md' }) => {
  const [hovered, setHovered] = useState(0);

  const displayValue = readOnly ? value : (hovered || value);

  return (
    <div className="flex items-center gap-0.5" role="group" aria-label="Rating">
      {[1, 2, 3, 4, 5].map((star) => {
        // For fractional display (readOnly only)
        const filled = displayValue >= star;
        const half = !filled && displayValue >= star - 0.5;

        return (
          <button
            key={star}
            type="button"
            disabled={readOnly}
            onClick={() => !readOnly && onChange?.(star)}
            onMouseEnter={() => !readOnly && setHovered(star)}
            onMouseLeave={() => !readOnly && setHovered(0)}
            className={`transition-transform duration-100 leading-none
              ${!readOnly ? 'cursor-pointer hover:scale-125 active:scale-110' : 'cursor-default'}
              ${SIZES[size]}`}
            aria-label={`${star} star${star !== 1 ? 's' : ''}`}
          >
            <span className={`
              ${filled    ? 'text-yellow-400' :
                half      ? 'text-yellow-200' :
                            'text-gray-300'}
              drop-shadow-sm`}>
              ★
            </span>
          </button>
        );
      })}
    </div>
  );
};

/**
 * RatingDisplay — compact rating badge (e.g. "⭐ 4.8 (127 reviews)")
 */
export const RatingDisplay = ({ rating, count, size = 'sm' }) => {
  if (!rating || rating === 0) {
    return <span className="text-gray-400 text-xs">No ratings yet</span>;
  }

  return (
    <div className="flex items-center gap-1.5">
      <StarRating value={rating} readOnly size={size} />
      <span className="font-semibold text-gray-700 text-sm">
        {rating.toFixed(1)}
      </span>
      {count !== undefined && (
        <span className="text-gray-400 text-xs">({count} reviews)</span>
      )}
    </div>
  );
};

export default StarRating;
