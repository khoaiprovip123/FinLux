'use client';

import React, { useEffect, useRef } from 'react';

export default function PrismEffects() {
  const cursorOrbRef = useRef<HTMLDivElement>(null);
  const secondaryOrbRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let mouseX = window.innerWidth / 2;
    let mouseY = window.innerHeight / 2;
    let orbX = mouseX;
    let orbY = mouseY;
    let secX = mouseX;
    let secY = mouseY;
    let rafId: number;

    // 1. Mouse coordinates listener
    const handleMouseMove = (e: MouseEvent) => {
      mouseX = e.clientX;
      mouseY = e.clientY;

      // Card-specific coordinates for hover spotlight
      const target = (e.target as HTMLElement)?.closest<HTMLElement>('.prism-glass-card');
      if (target) {
        const rect = target.getBoundingClientRect();
        target.style.setProperty('--mouse-x', `${e.clientX - rect.left}px`);
        target.style.setProperty('--mouse-y', `${e.clientY - rect.top}px`);
      }
    };

    // 2. Smooth trailing animation for cursor orbs
    const animateOrbs = () => {
      // Primary cyan orb (fast lag)
      orbX += (mouseX - orbX) * 0.18;
      orbY += (mouseY - orbY) * 0.18;

      // Secondary indigo aurora orb (gentle trailing float)
      secX += (mouseX - secX) * 0.08;
      secY += (mouseY - secY) * 0.08;

      if (cursorOrbRef.current) {
        cursorOrbRef.current.style.transform = `translate3d(${orbX}px, ${orbY}px, 0) translate(-50%, -50%)`;
      }
      if (secondaryOrbRef.current) {
        secondaryOrbRef.current.style.transform = `translate3d(${secX}px, ${secY}px, 0) translate(-50%, -50%)`;
      }

      rafId = requestAnimationFrame(animateOrbs);
    };

    rafId = requestAnimationFrame(animateOrbs);

    // 3. Water-Drop Shockwave Ripple on Click
    const handleClick = (e: MouseEvent) => {
      const ripple = document.createElement('div');
      ripple.className = 'prism-click-ripple';
      ripple.style.left = `${e.clientX}px`;
      ripple.style.top = `${e.clientY}px`;
      document.body.appendChild(ripple);

      // Micro ripple wave
      const innerWave = document.createElement('div');
      innerWave.className = 'prism-inner-wave';
      innerWave.style.left = `${e.clientX}px`;
      innerWave.style.top = `${e.clientY}px`;
      document.body.appendChild(innerWave);

      setTimeout(() => {
        ripple.remove();
        innerWave.remove();
      }, 700);
    };

    window.addEventListener('mousemove', handleMouseMove, { passive: true });
    window.addEventListener('click', handleClick);

    return () => {
      cancelAnimationFrame(rafId);
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('click', handleClick);
    };
  }, []);

  return (
    <>
      {/* Dynamic Luminous Floating Aurora Orbs (Smooth follow, high visual impact) */}
      <div
        ref={cursorOrbRef}
        className="pointer-events-none fixed top-0 left-0 w-[420px] h-[420px] rounded-full z-[1] transition-opacity duration-300 opacity-75"
        style={{
          background:
            'radial-gradient(circle, rgba(56, 189, 248, 0.22) 0%, rgba(37, 99, 235, 0.12) 38%, transparent 70%)',
          willChange: 'transform',
          filter: 'blur(35px)',
        }}
      />
      <div
        ref={secondaryOrbRef}
        className="pointer-events-none fixed top-0 left-0 w-[550px] h-[550px] rounded-full z-[1] transition-opacity duration-500 opacity-60"
        style={{
          background:
            'radial-gradient(circle, rgba(168, 85, 247, 0.12) 0%, rgba(56, 189, 248, 0.08) 45%, transparent 70%)',
          willChange: 'transform',
          filter: 'blur(45px)',
        }}
      />
    </>
  );
}
