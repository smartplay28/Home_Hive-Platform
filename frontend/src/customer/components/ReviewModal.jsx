/**
 * ReviewModal — slide-up modal for submitting a review after order completion.
 *
 * Features:
 *   - 5-star interactive rating
 *   - Optional text comment
 *   - Aspect tagging (positive + negative)
 *   - Duplicate detection (won't let you re-submit)
 */

import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { StarRating } from '../../../components/StarRating';
import api from '../../../lib/api';

const POSITIVE_ASPECTS = ['punctuality', 'quality', 'cleanliness', 'communication'];
const NEGATIVE_ASPECTS = ['punctuality', 'quality', 'cleanliness', 'pricing', 'communication'];

const AspectChip = ({ label, selected, color, onClick }) => (
  <button
    type="button"
    onClick={() => onClick(label)}
    className={`px-3 py-1 rounded-full text-xs font-medium border transition-all duration-150
      ${selected
        ? color === 'green'
          ? 'bg-green-500 text-white border-green-500'
          : 'bg-red-500 text-white border-red-500'
        : 'bg-white text-gray-600 border-gray-300 hover:border-gray-400'}`}
  >
    {label}
  </button>
);

const ReviewModal = ({ order, agentId, serviceId, onClose, onSubmitted }) => {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [positiveAspects, setPositiveAspects] = useState([]);
  const [negativeAspects, setNegativeAspects] = useState([]);
  const [loading, setLoading] = useState(false);

  const toggleAspect = (aspect, list, setList) => {
    setList(prev =>
      prev.includes(aspect) ? prev.filter(a => a !== aspect) : [...prev, aspect]
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0) {
      toast.warning('Please select a star rating.');
      return;
    }

    setLoading(true);
    try {
      await api.post('/reviews', {
        orderId: order.orderId,
        agentId,
        serviceId,
        rating,
        comment,
        positiveAspects,
        negativeAspects,
      });

      toast.success('Thank you for your review! ⭐');
      onSubmitted?.();
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to submit review. Please try again.';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/50 backdrop-blur-sm"
      onClick={(e) => e.target === e.currentTarget && onClose()}>

      <div className="bg-white w-full sm:max-w-md rounded-t-3xl sm:rounded-2xl p-6 shadow-2xl
        animate-[slideUp_0.3s_ease-out]">

        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-xl font-bold text-gray-800">Rate your experience</h2>
            <p className="text-sm text-gray-500 mt-0.5">Order #{order.orderId}</p>
          </div>
          <button onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 text-gray-500">
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Star Rating */}
          <div className="flex flex-col items-center py-4 bg-gray-50 rounded-2xl">
            <p className="text-sm text-gray-500 mb-3">How was the service?</p>
            <StarRating value={rating} onChange={setRating} size="lg" />
            <p className="text-sm font-medium text-gray-600 mt-2">
              {rating === 0 ? 'Tap to rate' :
               rating === 1 ? 'Poor' :
               rating === 2 ? 'Fair' :
               rating === 3 ? 'Good' :
               rating === 4 ? 'Great' : 'Excellent! ✨'}
            </p>
          </div>

          {/* Positive Aspects */}
          {rating >= 3 && (
            <div>
              <p className="text-sm font-semibold text-gray-700 mb-2">
                👍 What did you like?
              </p>
              <div className="flex flex-wrap gap-2">
                {POSITIVE_ASPECTS.map(aspect => (
                  <AspectChip
                    key={aspect}
                    label={aspect}
                    selected={positiveAspects.includes(aspect)}
                    color="green"
                    onClick={(a) => toggleAspect(a, positiveAspects, setPositiveAspects)}
                  />
                ))}
              </div>
            </div>
          )}

          {/* Negative Aspects */}
          {rating <= 3 && rating > 0 && (
            <div>
              <p className="text-sm font-semibold text-gray-700 mb-2">
                👎 What could be improved?
              </p>
              <div className="flex flex-wrap gap-2">
                {NEGATIVE_ASPECTS.map(aspect => (
                  <AspectChip
                    key={aspect}
                    label={aspect}
                    selected={negativeAspects.includes(aspect)}
                    color="red"
                    onClick={(a) => toggleAspect(a, negativeAspects, setNegativeAspects)}
                  />
                ))}
              </div>
            </div>
          )}

          {/* Comment */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1.5">
              Add a comment <span className="font-normal text-gray-400">(optional)</span>
            </label>
            <textarea
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              maxLength={500}
              rows={3}
              placeholder="Share your experience..."
              className="w-full px-3 py-2 border border-gray-300 rounded-xl text-sm
                focus:ring-2 focus:ring-[#1c4e80] focus:border-transparent outline-none resize-none"
            />
            <p className="text-xs text-gray-400 text-right mt-0.5">{comment.length}/500</p>
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={loading || rating === 0}
            className="w-full bg-[#1c4e80] text-white py-3 rounded-xl font-semibold
              hover:bg-[#153a61] transition-all duration-200
              disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Submitting...' : 'Submit Review'}
          </button>
        </form>
      </div>

      <style>{`
        @keyframes slideUp {
          from { transform: translateY(100%); opacity: 0; }
          to   { transform: translateY(0);    opacity: 1; }
        }
      `}</style>
    </div>
  );
};

export default ReviewModal;
