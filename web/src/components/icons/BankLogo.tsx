'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { FinancialInstitution, findInstitutionForWallet } from '@/lib/banks';
import { WalletType } from '@/types/finance';

interface BankLogoProps {
  walletName?: string;
  bankName?: string;
  walletType?: WalletType;
  customColor?: string;
  institution?: FinancialInstitution | null;
  size?: number;
  className?: string;
}

export default function BankLogo({
  walletName,
  bankName,
  walletType,
  customColor,
  institution: propInstitution,
  size = 40,
  className = '',
}: BankLogoProps) {
  const [imgError, setImgError] = useState(false);

  const matched =
    propInstitution || findInstitutionForWallet(walletName, bankName);

  const color = customColor || matched?.color || '#655BDC';
  const logoUrl = matched?.logoUrl;

  // Render official bank / e-wallet brand logo if available
  if (logoUrl && !imgError) {
    const iconSize = Math.round(size * 0.75);
    return (
      <span
        className={`fx-bank-logo ${className}`}
        style={{
          width: `${size}px`,
          height: `${size}px`,
          borderRadius: `${Math.round(size * 0.28)}px`,
          backgroundColor: '#FFFFFF',
          border: '1px solid var(--border)',
          boxShadow: '0 2px 6px rgba(0,0,0,0.04)',
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          overflow: 'hidden',
          flexShrink: 0,
          padding: `${Math.max(2, Math.round(size * 0.12))}px`,
        }}
        title={matched?.fullName || walletName}
      >
        <Image
          src={logoUrl}
          alt={matched?.shortName || walletName || 'Ngân hàng'}
          width={iconSize}
          height={iconSize}
          style={{
            objectFit: 'contain',
            width: '100%',
            height: '100%',
          }}
          onError={() => setImgError(true)}
        />
      </span>
    );
  }

  // Monogram or Generic icon with branded background
  const label = (matched?.code || walletName || 'VL').slice(0, 3).toUpperCase();

  return (
    <span
      className={`fx-bank-logo ${className}`}
      style={{
        width: `${size}px`,
        height: `${size}px`,
        borderRadius: `${Math.round(size * 0.28)}px`,
        backgroundColor: color,
        background: `linear-gradient(135deg, ${color} 0%, ${color}dd 100%)`,
        color: '#FFFFFF',
        border: '1px solid rgba(255,255,255,0.2)',
        boxShadow: `0 3px 8px ${color}33`,
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontWeight: 800,
        fontSize: `${Math.round(size * 0.34)}px`,
        letterSpacing: '-0.3px',
        flexShrink: 0,
      }}
      title={matched?.fullName || walletName}
    >
      {label}
    </span>
  );
}
