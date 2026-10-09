'use client';

import React, { useState } from 'react';
import { useFinance } from '@/context/FinanceContext';
import { SAVING_SPIN_PRIZES } from '@/lib/constants';
import { formatCurrency } from '@/lib/formatters';
import { X, Sparkles, Trophy } from 'lucide-react';
import confetti from 'canvas-confetti';

interface SavingSpinModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function SavingSpinModal({ isOpen, onClose }: SavingSpinModalProps) {
  const { addSpinReward } = useFinance();
  const [isSpinning, setIsSpinning] = useState(false);
  const [rotation, setRotation] = useState(0);
  const [wonPrize, setWonPrize] = useState<(typeof SAVING_SPIN_PRIZES)[0] | null>(null);

  if (!isOpen) return null;

  const handleSpin = () => {
    if (isSpinning) return;

    setIsSpinning(true);
    setWonPrize(null);

    const prizeIndex = Math.floor(Math.random() * SAVING_SPIN_PRIZES.length);
    const selected = SAVING_SPIN_PRIZES[prizeIndex];

    const sliceAngle = 360 / SAVING_SPIN_PRIZES.length;
    const targetAngle = 360 * 5 + (360 - prizeIndex * sliceAngle - sliceAngle / 2);

    const newRotation = rotation + targetAngle;
    setRotation(newRotation);

    setTimeout(() => {
      setIsSpinning(false);
      setWonPrize(selected);

      confetti({
        particleCount: 90,
        spread: 65,
        origin: { y: 0.6 },
      });

      if (selected.amount > 0) {
        addSpinReward(selected.amount, selected.label);
      }
    }, 4000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-md animate-in fade-in duration-150">
      <div className="w-full max-w-md bg-white/95 rounded-3xl border border-white p-6 shadow-2xl relative text-center prism-page-enter">
        {/* Close button */}
        <button
          onClick={onClose}
          className="absolute right-4 top-4 w-8 h-8 rounded-xl bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-400 hover:text-slate-700 transition-colors"
        >
          <X className="w-4 h-4" />
        </button>

        {/* Title */}
        <div className="mb-3">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-50 text-amber-600 text-xs font-bold border border-amber-200 mb-2">
            <Sparkles className="w-3.5 h-3.5" />
            <span>FinLux Prism Gamification</span>
          </div>
          <h2 className="text-lg font-black text-slate-900">Saving Spin — Vòng Quay Tiết Kiệm</h2>
          <p className="text-xs text-slate-500 mt-1 font-medium">
            Mỗi ngày 1 lượt quay để tạo thói quen tích lũy tài chính
          </p>
        </div>

        {/* Wheel Graphic */}
        <div className="relative w-56 h-56 mx-auto my-5 flex items-center justify-center">
          {/* Wheel Pointer */}
          <div className="absolute -top-3 left-1/2 -translate-x-1/2 z-20 w-0 h-0 border-l-[10px] border-l-transparent border-r-[10px] border-r-transparent border-t-[18px] border-t-amber-500 filter drop-shadow-[0_2px_6px_rgba(245,158,11,0.6)]" />

          {/* Rotating Wheel */}
          <div
            className="w-full h-full rounded-full border-4 border-white shadow-xl overflow-hidden relative"
            style={{
              transform: `rotate(${rotation}deg)`,
              transition: isSpinning ? 'transform 4s cubic-bezier(0.15, 0.9, 0.25, 1)' : 'none',
              background: 'conic-gradient(#10B981 0deg 60deg, #06B6D4 60deg 120deg, #F59E0B 120deg 180deg, #8B5CF6 180deg 240deg, #2563EB 240deg 300deg, #F43F5E 300deg 360deg)',
            }}
          >
            <div className="absolute inset-5 rounded-full bg-white border border-slate-100 flex items-center justify-center shadow-inner">
              <span className="font-black text-blue-600 text-xs tracking-wider">FINLUX</span>
            </div>
          </div>
        </div>

        {/* Won Prize Banner */}
        {wonPrize && (
          <div className="my-3 p-3.5 rounded-2xl bg-amber-50/80 border border-amber-200 text-center animate-in zoom-in-95 duration-150">
            <div className="flex items-center justify-center gap-1.5 text-amber-700 font-bold text-xs mb-0.5">
              <Trophy className="w-3.5 h-3.5" />
              <span>{wonPrize.label}</span>
            </div>
            <p className="text-[11px] text-slate-600">{wonPrize.message}</p>
            {wonPrize.amount > 0 && (
              <span className="inline-block mt-1 text-[11px] text-emerald-600 font-bold bg-emerald-50 px-2.5 py-0.5 rounded-full border border-emerald-200">
                Đã cộng {formatCurrency(wonPrize.amount)} vào ví mặc định!
              </span>
            )}
          </div>
        )}

        {/* Spin CTA */}
        <button
          onClick={handleSpin}
          disabled={isSpinning}
          className="w-full py-2.5 px-5 rounded-2xl font-bold text-xs text-white bg-blue-600 hover:bg-blue-500 shadow-md shadow-blue-500/25 disabled:opacity-50 transition-all active:scale-[0.98]"
        >
          {isSpinning ? 'Đang quay...' : 'Quay Vòng May Mắn 🎯'}
        </button>
      </div>
    </div>
  );
}
