import React from 'react';
import Image from 'next/image';

// ============================================================================
// FINLUX PRISM ICON SYSTEM & OFFICIAL LOGO
// ============================================================================

export const ICON_PATHS: Record<string, React.ReactNode> = {
  home: (
    <>
      <rect x="3" y="3" width="7" height="7" rx="1" />
      <rect x="14" y="3" width="7" height="7" rx="1" />
      <rect x="3" y="14" width="7" height="7" rx="1" />
      <rect x="14" y="14" width="7" height="7" rx="1" />
    </>
  ),
  swap: (
    <path d="M7 7h14m0 0-4-4m4 4-4 4M17 17H3m0 0 4-4m-4 4 4 4" />
  ),
  wallet: (
    <>
      <rect x="3" y="6" width="18" height="15" rx="3" />
      <path d="M16 11h5M5 6V4a2 2 0 0 1 2-2h11" />
      <circle cx="16" cy="15" r="1" />
    </>
  ),
  budget: (
    <>
      <rect x="3" y="3" width="18" height="18" rx="3" />
      <path d="M8 14v3m4-7v7m4-10v10" />
    </>
  ),
  chart: (
    <path d="M3 3v18h18M7 15l4-5 4 2 5-7" />
  ),
  target: (
    <>
      <circle cx="12" cy="12" r="9" />
      <circle cx="12" cy="12" r="5" />
      <circle cx="12" cy="12" r="1" />
    </>
  ),
  gear: (
    <>
      <circle cx="12" cy="12" r="3" />
      <path d="M19 13.5v-3l2-1.5-2-3.5-2.5 1-2.5-1.5-.5-2.5h-4L9 5 6.5 6.5 4 5.5 2 9l2 1.5v3L2 15l2 3.5 2.5-1L9 19l.5 2.5h4L14 19l2.5-1.5 2.5 1L21 15z" />
    </>
  ),
  plus: (
    <path d="M12 5v14M5 12h14" />
  ),
  moon: (
    <path d="M20.5 14.4A9 9 0 0 1 9.6 3.5 9 9 0 1 0 20.5 14.4z" />
  ),
  sun: (
    <>
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2m0 16v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
    </>
  ),
  menu: (
    <path d="M4 6h16M4 12h16M4 18h16" />
  ),
  close: (
    <path d="M5 5l14 14M19 5L5 19" />
  ),
  up: (
    <path d="M12 20V4M5 11l7-7 7 7" />
  ),
  down: (
    <path d="M12 4v16m-7-7 7 7 7-7" />
  ),
  arrow: (
    <path d="M5 12h14m-6-6 6 6-6 6" />
  ),
  search: (
    <>
      <circle cx="10.5" cy="10.5" r="7" />
      <path d="m16 16 5 5" />
    </>
  ),
  receipt: (
    <>
      <path d="M5 3h14v18l-3-2-4 2-4-2-3 2zM9 8h6M9 12h6" />
    </>
  ),
  spark: (
    <path d="m12 2 2.6 7.4L22 12l-7.4 2.6L12 22l-2.6-7.4L2 12l7.4-2.6z" />
  ),
  shield: (
    <>
      <path d="M12 2 4 5v7c0 5 3 8 8 10 5-2 8-5 8-10V5z" />
      <path d="m9 12 2 2 4-4" />
    </>
  ),
  calendar: (
    <>
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <path d="M7 2v6m10-6v6M3 10h18" />
    </>
  ),
  check: (
    <path d="m4 12 5 5L20 6" />
  ),
  trash: (
    <>
      <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
    </>
  ),
  file: (
    <>
      <path d="M6 2h8l5 5v15H6zM14 2v5h5M9 12h7M9 16h7" />
    </>
  ),
};

export interface FinluxIconProps {
  name: string;
  className?: string;
}

export function FinluxIcon({ name, className = 'fx-icon' }: FinluxIconProps) {
  const content = ICON_PATHS[name] || ICON_PATHS.home;
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden="true">
      {content}
    </svg>
  );
}

// Official FinLux Brand Logo (App Icon with squircle bo viền)
export function FinluxLogo({
  className = 'fx-logo',
  size = 43,
  rounded,
  style,
}: {
  className?: string;
  size?: number;
  rounded?: number;
  style?: React.CSSProperties;
}) {
  const isCustomSize = size !== 43;
  const radius = rounded ?? (size >= 64 ? 22 : size >= 48 ? 16 : 14);

  return (
    <span
      className={className}
      style={{
        width: isCustomSize ? `${size}px` : undefined,
        height: isCustomSize ? `${size}px` : undefined,
        minWidth: isCustomSize ? `${size}px` : undefined,
        minHeight: isCustomSize ? `${size}px` : undefined,
        borderRadius: `${radius}px`,
        overflow: 'hidden',
        padding: 0,
        position: 'relative',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        ...style,
      }}
    >
      <Image
        src="/finlux_logo.png"
        alt="FinLux App Logo"
        width={size}
        height={size}
        priority
        style={{
          width: '100%',
          height: '100%',
          objectFit: 'contain',
          borderRadius: `${radius}px`,
        }}
      />
    </span>
  );
}


// Wave Logo export for backwards compatibility
export function IconWaveLogo({ className = 'w-7 h-7' }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 36 36" fill="none">
      <defs>
        <linearGradient id="finlux_wave_primary" x1="4" y1="24" x2="32" y2="12" gradientUnits="userSpaceOnUse">
          <stop stopColor="#8E79FF" />
          <stop offset="0.5" stopColor="#6159DB" />
          <stop offset="1" stopColor="#4BBFDD" />
        </linearGradient>
      </defs>
      <path
        d="M5 23C9 18 13.5 15 18 19C22.5 23 27 21 31 16"
        stroke="url(#finlux_wave_primary)"
        strokeWidth="4.2"
        strokeLinecap="round"
      />
      <circle cx="8" cy="27" r="2.8" fill="#4BBFDD" />
      <circle cx="28" cy="11" r="3.2" fill="#8E79FF" />
    </svg>
  );
}

// Backwards compatibility icon components
export const IconNavHome = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="home" className={className} />;
export const IconNavIncome = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="down" className={className} />;
export const IconNavExpense = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="up" className={className} />;
export const IconNavHistory = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="swap" className={className} />;
export const IconNavReports = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="chart" className={className} />;
export const IconNavPlan = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="budget" className={className} />;
export const IconNavAi = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="spark" className={className} />;
export const IconNavSettings = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="gear" className={className} />;
export const IconNavSpin = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="target" className={className} />;
export const IconAddIncome = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="down" className={className} />;
export const IconAddExpense = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="up" className={className} />;
export const IconTransfer = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="swap" className={className} />;
export const IconSpin = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="target" className={className} />;
export const IconPlan = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="budget" className={className} />;
export const IconReports = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="chart" className={className} />;
export const IconGoalMotorbike = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="target" className={className} />;
export const IconGoalTravel = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="target" className={className} />;
export const IconGoalEmergency = ({ className = 'fx-icon' }: { className?: string }) => <FinluxIcon name="shield" className={className} />;
