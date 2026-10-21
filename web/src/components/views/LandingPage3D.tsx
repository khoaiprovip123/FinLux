'use client';

import React, { useState, useEffect, useRef, useCallback, useSyncExternalStore } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { useFinance } from '@/context/FinanceContext';
import * as THREE from 'three';
import { createProceduralScreenTexture } from './landingScreens';

const DESKTOP_QUERY = '(min-width: 901px)';
const STAGE_LAYOUT = {
  modelScale: 1.15,
  framingMargin: 1.06,
  labelEdgeGap: 16,
  ambientMinRadius: 1.9,
  ambientRadiusRange: 0.7,
  ambientBobAmplitude: 0.15,
} as const;
function subscribeDesktop(listener: () => void) {
  const query = window.matchMedia(DESKTOP_QUERY);
  query.addEventListener('change', listener);
  return () => query.removeEventListener('change', listener);
}
const desktopSnapshot = () => window.matchMedia(DESKTOP_QUERY).matches;
const serverDesktopSnapshot = () => false;

export default function LandingPage3D() {
  const { theme, toggleTheme } = useFinance();
  const render3d = useSyncExternalStore(subscribeDesktop, desktopSnapshot, serverDesktopSnapshot);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [noGl, setNoGl] = useState(false);
  const [activeScene, setActiveScene] = useState(0);

  const scrollTo = useCallback((id: string) => {
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
  }, []);

  useEffect(() => {
    const cv = canvasRef.current;
    if (!cv || !render3d) return;

    let renderer: THREE.WebGLRenderer;
    try {
      renderer = new THREE.WebGLRenderer({
        canvas: cv,
        alpha: true,
        antialias: true,
        powerPreference: 'high-performance',
      });
    } catch {
      queueMicrotask(() => setNoGl(true));
      return;
    }

    // Set sRGB / color management
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));

    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const scene = new THREE.Scene();
    const cam = new THREE.PerspectiveCamera(40, 1, 0.1, 100);
    cam.position.set(0, 0, 8.5);

    // Lights
    scene.add(new THREE.AmbientLight(0xffffff, 0.7));
    const l1 = new THREE.PointLight(0x7d7cff, 1.6, 40);
    l1.position.set(-5, 4, 6);
    scene.add(l1);

    const l2 = new THREE.PointLight(0x5bd7ff, 1.3, 40);
    l2.position.set(5, -3, 5);
    scene.add(l2);

    const l3 = new THREE.DirectionalLight(0xffffff, 0.7);
    l3.position.set(0, 5, 5);
    scene.add(l3);

    // Material & shape helpers
    function gm(color: number, opacity = 0.92) {
      return new THREE.MeshPhysicalMaterial({
        color,
        roughness: 0.12,
        metalness: 0.15,
        clearcoat: 1.0,
        clearcoatRoughness: 0.1,
        transparent: true,
        opacity,
      });
    }

    function rr(w: number, h: number, r: number) {
      const s = new THREE.Shape();
      const x = -w / 2;
      const y = -h / 2;
      s.moveTo(x + r, y);
      s.lineTo(x + w - r, y);
      s.quadraticCurveTo(x + w, y, x + w, y + r);
      s.lineTo(x + w, y + h - r);
      s.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
      s.lineTo(x + r, y + h);
      s.quadraticCurveTo(x, y + h, x, y + h - r);
      s.lineTo(x, y + r);
      s.quadraticCurveTo(x, y, x + r, y);
      return s;
    }

    const root = new THREE.Group();
    scene.add(root);

    const gP = new THREE.Group();
    const gW = new THREE.Group();
    gP.scale.setScalar(0.001);
    gW.scale.setScalar(0.001);
    root.add(gP, gW);

    // Phone: rounded body + rounded screen showing real app screenshots
    const SW = 1.5;
    const SH = 3.333;
    const bodyGeo = new THREE.ExtrudeGeometry(rr(SW + 0.16, SH + 0.16, 0.26), {
      depth: 0.12,
      bevelEnabled: true,
      bevelThickness: 0.03,
      bevelSize: 0.03,
      bevelSegments: 3,
      curveSegments: 14,
    });
    bodyGeo.center();
    const bodyMat = new THREE.MeshStandardMaterial({
      color: 0x10182e,
      metalness: 0.6,
      roughness: 0.28,
    });
    const body = new THREE.Mesh(bodyGeo, bodyMat);
    gP.add(body);

    // Screen ShapeGeometry with custom UV mapping
    const sg = new THREE.ShapeGeometry(rr(SW, SH, 0.18), 14);
    const pos = sg.attributes.position;
    const uv = sg.attributes.uv;
    for (let i = 0; i < pos.count; i++) {
      uv.setXY(i, (pos.getX(i) + SW / 2) / SW, (pos.getY(i) + SH / 2) / SH);
    }

    // Load actual real screenshots of FinLux native Android app
    const loader = new THREE.TextureLoader();
    const realScreens = [
      '/screenshots/qa-home.png',
      '/screenshots/qa-budget.png',
      '/screenshots/qa-reports.png',
      '/screenshots/qa-login.png',
    ];

    const texList = realScreens.map((url, i) => {
      const fallbackUrl = createProceduralScreenTexture(
        i === 0 ? 'home' : i === 1 ? 'budget' : i === 2 ? 'reports' : 'saving'
      );
      const t = loader.load(
        url,
        () => {
          t.needsUpdate = true;
        },
        undefined,
        () => {
          // Fallback if network issue
          t.image.src = fallbackUrl;
          t.needsUpdate = true;
        }
      );
      t.colorSpace = THREE.SRGBColorSpace;
      t.anisotropy = 4;
      return t;
    });

    const mA = new THREE.MeshBasicMaterial({ map: texList[0] });
    const mB = new THREE.MeshBasicMaterial({ map: texList[0], transparent: true, opacity: 0 });
    const sA = new THREE.Mesh(sg, mA);
    const sB = new THREE.Mesh(sg, mB);
    sA.position.z = 0.121;
    sB.position.z = 0.123;
    gP.add(sA, sB);

    // Wheel for the Saving Spin scene
    const wc = [0x7d7cff, 0x5bd7ff, 0x58d68d, 0xffd166, 0xff7e88];
    const wheel = new THREE.Group();
    gW.add(wheel);

    wc.forEach((c, i) => {
      const arc = (Math.PI * 2) / 5;
      const m = new THREE.Mesh(new THREE.TorusGeometry(1.7, 0.34, 32, 64, arc - 0.14), gm(c));
      m.rotation.z = i * arc + 0.07;
      wheel.add(m);
    });

    const hub = new THREE.Mesh(
      new THREE.CylinderGeometry(0.7, 0.7, 0.16, 56),
      new THREE.MeshStandardMaterial({ color: 0xffd166, metalness: 0.7, roughness: 0.25 })
    );
    hub.rotation.x = Math.PI / 2;
    wheel.add(hub);

    const pt = new THREE.Mesh(new THREE.ConeGeometry(0.2, 0.45, 20), gm(0xffffff, 1));
    pt.position.y = 2.3;
    pt.rotation.z = Math.PI;
    gW.add(pt);

    // Scene groups mapping & yaw angles
    const gs = [gP, gP, gP, gP, gP, gW, gP, gP, gP];
    const imgMap = [0, 0, 0, 1, 2, 0, 3, 0, 3];
    const yaw = [-0.35, 0.4, -0.3, -0.4, 0.4, 0, -0.3, 0.35, -0.3];

    // Ambient floating orbs
    const orbs: THREE.Mesh[] = [];
    const og = new THREE.SphereGeometry(1, 24, 24);
    for (let k = 0; k < 9; k++) {
      const s = 0.08 + Math.random() * 0.14;
      const o = new THREE.Mesh(og, gm(wc[k % 5], 0.75));
      o.scale.setScalar(s);
      o.userData = {
        r: STAGE_LAYOUT.ambientMinRadius + Math.random() * STAGE_LAYOUT.ambientRadiusRange,
        t: Math.random() * 6.28,
        sp: 0.15 + Math.random() * 0.3,
        y: (Math.random() - 0.5) * 2.8,
      };
      scene.add(o);
      orbs.push(o);
    }

    // A rotation-invariant envelope keeps the whole model inside the stage at any aspect ratio.
    gP.scale.setScalar(1);
    gW.scale.setScalar(1);
    root.updateMatrixWorld(true);
    const meshSphere = new THREE.Sphere();
    let modelRadius = 0;
    root.traverse((object) => {
      if (!(object instanceof THREE.Mesh)) return;
      object.geometry.computeBoundingSphere();
      meshSphere.copy(object.geometry.boundingSphere!).applyMatrix4(object.matrixWorld);
      modelRadius = Math.max(modelRadius, meshSphere.center.length() + meshSphere.radius);
    });
    gP.scale.setScalar(0.001);
    gW.scale.setScalar(0.001);
    const stageRadius = Math.max(modelRadius * STAGE_LAYOUT.modelScale, ...orbs.map((orb) => {
      const data = orb.userData as { r: number; y: number };
      return Math.hypot(data.r, Math.abs(data.y) + STAGE_LAYOUT.ambientBobAmplitude) + orb.scale.x;
    }));

    let tx = 0;
    let ty = 0;
    let vx = 0;
    let vy = 0;
    let rx = 0;
    let ry = 0;
    let drag = false;
    let lx = 0;
    let ly = 0;
    let px = 0;
    let py = 0;
    let W = window.innerWidth;
    let H = window.innerHeight;
    let curImg = 0;
    let fade = 1;
    let currentSceneIdx = 0;

    function size() {
      const bounds = cv!.getBoundingClientRect();
      W = bounds.width;
      H = bounds.height;
      renderer.setSize(W, H, false);
      cam.aspect = W / H;
      const halfFov = THREE.MathUtils.degToRad(cam.fov / 2);
      const limitingHalfFov = Math.atan(Math.tan(halfFov) * Math.min(1, cam.aspect));
      cam.position.z = stageRadius * STAGE_LAYOUT.framingMargin / Math.sin(limitingHalfFov);
      cam.updateProjectionMatrix();
    }
    window.addEventListener('resize', size);
    size();

    const onPointerMove = (e: MouseEvent) => {
      px = (e.clientX - cv.getBoundingClientRect().left) / W - 0.5;
      py = e.clientY / H - 0.5;
      if (drag) {
        vy = (e.clientX - lx) * 0.01;
        vx = (e.clientY - ly) * 0.01;
        lx = e.clientX;
        ly = e.clientY;
      }
    };

    const onPointerDown = (e: MouseEvent) => {
      drag = true;
      lx = e.clientX;
      ly = e.clientY;
      cv.classList.add('drag');
    };

    const onPointerUp = () => {
      drag = false;
      cv.classList.remove('drag');
    };

    window.addEventListener('pointermove', onPointerMove);
    cv.addEventListener('pointerdown', onPointerDown);
    window.addEventListener('pointerup', onPointerUp);

    // Scroll Scrubbing & IntersectionObserver
    const sc = Array.from(document.querySelectorAll<HTMLElement>('.scene'));
    const dots = Array.from(document.querySelectorAll<HTMLElement>('.rail a'));
    const scr = Array.from(document.querySelectorAll<HTMLElement>('[data-scrub]'));
    const styleNames = ['Modern Dark', 'Glassmorphism', 'Dynamic Gradient'];

    function cl(v: number) {
      return Math.max(0, Math.min(1, v));
    }

    function scrub() {
      const vh = window.innerHeight;
      scr.forEach((el) => {
        const sec = el.closest<HTMLElement>('.scene');
        if (!sec) return;
        const b = sec.getBoundingClientRect();
        const p = cl((vh * 0.75 - b.top) / (b.height * 0.75));
        const id = el.id;

        if (id === 'meter') {
          el.style.setProperty('--p', p.toString());
          el.classList.toggle('w', p >= 0.8 && p < 1);
          el.classList.toggle('x', p >= 1);
          const lbl = document.getElementById('mlabel');
          if (lbl) {
            lbl.textContent =
              p >= 1 ? 'Chạm hạn mức 100%' : p >= 0.8 ? 'Cảnh báo ở mức 80%' : 'Trong hạn mức';
          }
        } else if (id === 'tabs') {
          const k = Math.min(4, Math.floor(p * 5));
          Array.from(el.children).forEach((c, i) => {
            c.classList.toggle('on', i === k);
          });
        } else if (id === 'stack' || id === 'eq') {
          Array.from(el.children).forEach((c, i) => {
            (c as HTMLElement).style.setProperty('--q', cl(p * 3 - i * 0.7).toString());
          });
        } else if (id === 'sty') {
          const j = Math.min(2, Math.floor(p * 3));
          el.dataset.style = j.toString();
          const sname = document.getElementById('sname');
          if (sname) sname.textContent = styleNames[j];
          const indicators = el.querySelectorAll<HTMLElement>('.sw i');
          indicators.forEach((c, i) => {
            c.classList.toggle('on', i === j);
          });
        }
      });
    }

    function act() {
      const m = window.innerHeight / 2;
      let b = 0;
      sc.forEach((s, i) => {
        const q = s.getBoundingClientRect();
        if (q.top <= m && q.bottom > m) b = i;
      });
      currentSceneIdx = b;
      setActiveScene(b);
      dots.forEach((d, i) => {
        d.classList.toggle('on', i === b);
      });
      scrub();


    }

    window.addEventListener('scroll', act, { passive: true });
    act();

    // IntersectionObserver for slide / wipe effects
    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((e) => {
          if (e.isIntersecting) e.target.classList.add('in');
        });
      },
      { threshold: 0.25 }
    );
    document.querySelectorAll('.fx').forEach((el) => io.observe(el));

    // Animation loop
    const clock = new THREE.Timer();
    clock.connect(document);
    function L(v: number, target: number, k: number) {
      return v + (target - v) * k;
    }

    let animId: number;
    const pv = new THREE.Vector3();
    const oc = Array.from(document.querySelectorAll<HTMLElement>('.ochip'));

    function tick() {
      animId = requestAnimationFrame(tick);
      if (document.hidden) return;

      clock.update();
      const t = clock.getElapsed();
      const wide = window.innerWidth > 900;
      const idx = currentSceneIdx;
      const actGroup = gs[idx];

      const gx = 0;
      const gy = 0;
      const scScale = STAGE_LAYOUT.modelScale;

      root.position.x = L(root.position.x, gx, 0.06);
      root.position.y = L(root.position.y, gy, 0.06);
      root.scale.setScalar(L(root.scale.x, scScale, 0.06));

      [gP, gW].forEach((g) => {
        const s = L(g.scale.x, g === actGroup ? 1 : 0.001, 0.09);
        g.scale.setScalar(s);
        g.visible = s > 0.01;
      });

      // Crossfade screen textures when switching scenes
      const want = imgMap[idx];
      if (actGroup === gP && want !== curImg) {
        mB.map = texList[want];
        mB.needsUpdate = true;
        fade = 0;
        curImg = want;
      }
      if (fade < 1) {
        fade = Math.min(1, fade + 0.06);
        mB.opacity = fade;
        if (fade >= 1) {
          mA.map = texList[curImg];
          mA.needsUpdate = true;
          mB.opacity = 0;
        }
      }

      ty += vy;
      tx += vx;
      vy *= 0.93;
      vx *= 0.93;

      rx = L(rx, 0.12 + py * 0.25 + tx, 0.08);
      ry = L(ry, yaw[idx] + ty + px * 0.5, 0.06);
      root.rotation.x = rx;
      root.rotation.y = ry;

      gP.position.y = reduce ? 0 : Math.sin(t * 0.8) * 0.08;
      if (!reduce) wheel.rotation.z = -t * 0.7;

      // Orbiting spheres
      orbs.forEach((o) => {
        const d = o.userData as { r: number; t: number; sp: number; y: number };
        if (!reduce) d.t += d.sp * 0.01;
        o.position.set(
          Math.cos(d.t) * d.r + root.position.x,
          d.y + Math.sin(t * 0.6 + d.r) * STAGE_LAYOUT.ambientBobAmplitude + root.position.y,
          Math.sin(d.t) * d.r * 0.5
        );
      });

      // 3D Orbital Chips projected to 2D Screen Coordinates
      root.updateMatrixWorld(true);
      oc.forEach((el, i) => {
        const a = (reduce ? 0 : t * 0.22) + (i * Math.PI * 2) / oc.length;
        pv.set(Math.cos(a) * 2.5, Math.sin(a * 2) * 0.9 + (i - 2) * 0.55, Math.sin(a) * 1.4);
        gP.localToWorld(pv);
        const z = pv.z;
        pv.project(cam);
        const sx = (pv.x * 0.5 + 0.5) * W;
        const sy = (-pv.y * 0.5 + 0.5) * H;
        const show = idx === 0 && wide;
        const k = 0.82 + Math.max(-0.3, Math.min(0.5, z * 0.12));
        const halfWidth = el.offsetWidth * k / 2;
        const halfHeight = el.offsetHeight * k / 2;
        const x = THREE.MathUtils.clamp(sx, halfWidth + STAGE_LAYOUT.labelEdgeGap, W - halfWidth - STAGE_LAYOUT.labelEdgeGap);
        const y = THREE.MathUtils.clamp(sy, halfHeight + STAGE_LAYOUT.labelEdgeGap, H - halfHeight - STAGE_LAYOUT.labelEdgeGap);
        el.style.transform = `translate(${x.toFixed(1)}px,${y.toFixed(1)}px) translate(-50%,-50%) scale(${k.toFixed(3)})`;
        el.style.opacity = show ? (z > -0.4 ? '1' : '0.45') : '0';
      });

      renderer.render(scene, cam);
    }

    tick();

    return () => {
      cancelAnimationFrame(animId);
      clock.disconnect();
      window.removeEventListener('resize', size);
      window.removeEventListener('scroll', act);
      window.removeEventListener('pointermove', onPointerMove);
      cv.removeEventListener('pointerdown', onPointerDown);
      window.removeEventListener('pointerup', onPointerUp);
      io.disconnect();
      scene.traverse((object) => {
        if (object instanceof THREE.Mesh) {
          object.geometry.dispose();
          const materials = Array.isArray(object.material) ? object.material : [object.material];
          materials.forEach((material) => material.dispose());
        }
      });
      texList.forEach((texture) => texture.dispose());
      renderer.dispose();
      scene.clear();
    };
  }, [render3d]);

  return (
    <div className={`finlux-landing-root ${noGl ? 'nogl' : ''}`} data-theme={theme}>
      {/* 3D WEBGL STAGE CANVAS */}
      <canvas
        ref={canvasRef}
        id="stage"
        role="img"
        aria-label="Mô hình điện thoại 3D hiển thị các màn hình thật của ứng dụng FinLux; kéo để xoay"
      />

      {/* GLASSMORPHISM FLOATING HEADER NAVIGATION */}
      <header className="dock" aria-label="FinLux Navigation">
        <a
          className="logo"
          href="#s0"
          onClick={(e) => {
            e.preventDefault();
            scrollTo('s0');
          }}
        >
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src="/finlux_logo.png"
            alt="FinLux Logo"
            className="logo-icon"
            width={28}
            height={28}
          />
          <span className="logo-text">FINLUX</span>
          <span className="logo-badge">PRISM</span>
        </a>

        {/* Center Navigation Links */}
        <nav className="dock-nav" aria-label="Điều hướng nhanh">
          <a
            href="#s1"
            className={`dock-link ${activeScene >= 1 && activeScene <= 3 ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              scrollTo('s1');
            }}
          >
            Tính năng
          </a>
          <a
            href="#s4"
            className={`dock-link ${activeScene === 4 || activeScene === 5 ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              scrollTo('s4');
            }}
          >
            Báo cáo
          </a>
          <a
            href="#s7"
            className={`dock-link ${activeScene >= 6 ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              scrollTo('s7');
            }}
          >
            Bảo mật
          </a>
        </nav>

        {/* Right Actions */}
        <div className="dock-actions">
          <button
            className="btn-theme-toggle"
            id="theme"
            type="button"
            aria-label="Đổi chế độ Sáng / Tối"
            onClick={toggleTheme}
            title={theme === 'dark' ? 'Chuyển sang giao diện Sáng' : 'Chuyển sang giao diện Tối'}
          >
            {theme === 'dark' ? (
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
                <circle cx="12" cy="12" r="4" />
                <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
              </svg>
            ) : (
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
              </svg>
            )}
          </button>

          <Link
            href="/login"
            className="btn-dock-login"
          >
            <span>Đăng nhập</span>
          </Link>

          <Link
            href="/register"
            className="btn-dock-cta"
          >
            <span>Đăng ký ngay</span>
            <span className="cta-arrow">→</span>
          </Link>
        </div>
      </header>

      {/* SIDE RAIL NAVIGATION */}
      <nav className="rail" aria-label="Các phần của trang">
        <a href="#s0" aria-label="Tổng quan" onClick={(e) => { e.preventDefault(); scrollTo('s0'); }} className={activeScene === 0 ? 'on' : ''} />
        <a href="#s1" data-n="01" aria-label="Đa ví" onClick={(e) => { e.preventDefault(); scrollTo('s1'); }} className={activeScene === 1 ? 'on' : ''} />
        <a href="#s2" data-n="02" aria-label="Dòng tiền tự do" onClick={(e) => { e.preventDefault(); scrollTo('s2'); }} className={activeScene === 2 ? 'on' : ''} />
        <a href="#s3" data-n="03" aria-label="Ngân sách chu kỳ lương" onClick={(e) => { e.preventDefault(); scrollTo('s3'); }} className={activeScene === 3 ? 'on' : ''} />
        <a href="#s4" data-n="04" aria-label="Báo cáo" onClick={(e) => { e.preventDefault(); scrollTo('s4'); }} className={activeScene === 4 ? 'on' : ''} />
        <a href="#s5" data-n="05" aria-label="Tiết kiệm và Saving Spin" onClick={(e) => { e.preventDefault(); scrollTo('s5'); }} className={activeScene === 5 ? 'on' : ''} />
        <a href="#s6" data-n="06" aria-label="Phong cách giao diện" onClick={(e) => { e.preventDefault(); scrollTo('s6'); }} className={activeScene === 6 ? 'on' : ''} />
        <a href="#s7" data-n="07" aria-label="Độ tin cậy" onClick={(e) => { e.preventDefault(); scrollTo('s7'); }} className={activeScene === 7 ? 'on' : ''} />
        <a href="#s8" data-n="08" aria-label="Bắt đầu" onClick={(e) => { e.preventDefault(); scrollTo('s8'); }} className={activeScene === 8 ? 'on' : ''} />
      </nav>

      {/* 3D ORBITAL CHIPS */}
      <div id="orb" aria-hidden="true">
        <span className="ochip" style={{ '--c': 'var(--a2)' } as React.CSSProperties}>Đồng bộ Real-time Mobile &amp; Web</span>
        <span className="ochip" style={{ '--c': 'var(--a1)' } as React.CSSProperties}>Zero Double-Counting</span>
        <span className="ochip" style={{ '--c': 'var(--a3)' } as React.CSSProperties}>Bảo toàn Kế toán Kép</span>
        <span className="ochip" style={{ '--c': '#FFD166' } as React.CSSProperties}>Dòng tiền Tự do (Free Cash Flow)</span>
        <span className="ochip" style={{ '--c': '#FF7E88' } as React.CSSProperties}>Ngân sách Chu kỳ Lương</span>
      </div>

      <main>
        {/* SECTION 00: HERO */}
        <section className="scene hero" id="s0">
          <div className="copy">
            <span className="eb">Financial Architecture · Liquid Glass · System Invariants</span>
            <h1>FINLUX</h1>
            <div className="msg">
              Hiểu đúng dòng tiền, <em>làm chủ</em> tài chính.
            </div>
            <p>
              Quản trị đa ví, ngân sách chu kỳ lương và dòng tiền tự do — minh bạch theo chuẩn kế toán kép.
            </p>
            <blockquote className="pq">
              <p>“Sự giàu có thực sự không nằm ở những gì người khác nhìn thấy, mà nằm ở quyền tự do được đưa ra lựa chọn mỗi ngày.”</p>
              <cite>— Morgan Housel (Tâm lý học về tiền)</cite>
            </blockquote>
            <div className="btns">
              <Link
                className="btn p"
                href="/register"
              >
                Bắt đầu đăng ký miễn phí →
              </Link>
              <Link
                className="btn g"
                href="/login"
              >
                Đăng nhập tài khoản
              </Link>
            </div>
          </div>
          <div className="hero-preview">
            <Image src="/screenshots/qa-home.png" alt="Màn hình tổng quan FinLux trên điện thoại" width={270} height={600} priority />
          </div>
        </section>

        {/* SECTION 01: ĐA VÍ */}
        <section className="scene" id="s1">
          <div className="copy">
            <span className="eb">01 / Đa ví</span>
            <h2>
              Mọi dòng tiền, <em>một bức tranh</em>.
            </h2>
            <p className="li">Bạn không thiếu tiền. Bạn thiếu một nơi để nhìn thấy tất cả.</p>
            <p>
              Gom tiền mặt, ngân hàng, ví điện tử, thẻ tín dụng và đầu tư về cùng một chỗ. Chuyển tiền giữa các ví không bị tính là thu hay chi, đó là Zero Double-Counting.
            </p>
            <div className="chips fx fx-slide">
              <span>Tiền mặt</span>
              <span>Ngân hàng</span>
              <span>Ví điện tử</span>
              <span>Thẻ tín dụng</span>
              <span>Đầu tư</span>
            </div>
          </div>
        </section>

        {/* SECTION 02: DÒNG TIỀN TỰ DO */}
        <section className="scene" id="s2">
          <div className="copy">
            <span className="eb">02 / Dòng tiền tự do</span>
            <h2>
              Biết mình <em>thật sự được tiêu</em> bao nhiêu.
            </h2>
            <p className="li">Tự do không phải là có nhiều tiền, mà là biết chắc phần nào thuộc về mình.</p>
            <div className="stack" id="eq" data-scrub>
              <div><b>Thu nhập thực nhận</b></div>
              <div><b>− Chi phí thiết yếu</b></div>
              <div><b>− Tích lũy và trả nợ gốc</b></div>
              <div className="res">
                <b>= Dòng tiền tự do</b>
                <span>Phần bạn an tâm dùng cho sở thích, học tập, tái đầu tư.</span>
              </div>
            </div>
          </div>
        </section>

        {/* SECTION 03: NGÂN SÁCH CHU KỲ LƯƠNG */}
        <section className="scene" id="s3">
          <div className="copy">
            <span className="eb">03 / Ngân sách chu kỳ lương</span>
            <h2>
              Tiền chạy <em>theo nhịp lương</em>, không theo lịch.
            </h2>
            <p className="li">Cuối tháng không còn là nỗi sợ.</p>
            <p>
              Đặt hạn mức cho từng danh mục, nhận thông báo khi đạt 80% và 100%. Ngân sách chạy theo chu kỳ lương hoặc theo tháng.
            </p>
            <div className="meter" id="meter" data-scrub>
              <div className="track">
                <i />
                <b className="m80">80%</b>
                <b className="m100">100%</b>
              </div>
              <div className="mlabel" id="mlabel">Trong hạn mức</div>
            </div>
          </div>
        </section>

        {/* SECTION 04: BÁO CÁO */}
        <section className="scene" id="s4">
          <div className="copy">
            <span className="eb">04 / Báo cáo</span>
            <h2>
              Con số <em>biết kể chuyện</em>.
            </h2>
            <p className="li">Mỗi biểu đồ là một câu trả lời: tiền của tôi đã đi đâu?</p>
            <div className="tabs" id="tabs" data-scrub>
              <span>Tuần</span>
              <span className="on">Tháng</span>
              <span>Quý</span>
              <span>Năm</span>
              <span>Tùy chọn</span>
            </div>
            <p style={{ marginTop: '14px' }}>
              Biểu đồ đường xu hướng, donut tỷ trọng và treemap bất đối xứng.
            </p>
            <div className="exp">
              <span>Xuất Excel (.xlsx)</span>
              <span>Xuất PDF có biểu đồ</span>
            </div>
          </div>
        </section>

        {/* SECTION 05: TIẾT KIỆM */}
        <section className="scene" id="s5">
          <div className="copy">
            <span className="eb">05 / Tiết kiệm</span>
            <h2>
              Dành dụm cũng có thể <em>là niềm vui</em>.
            </h2>
            <p className="li">Thói quen lớn bắt đầu từ một khoản nhỏ.</p>
            <div className="stack" id="stack" data-scrub>
              <div>
                <b>Mục tiêu tiết kiệm</b>
                <span>Đặt khoản muốn dành dụm và theo dõi tiến độ.</span>
              </div>
              <div>
                <b>Theo dõi khoản nợ</b>
                <span>Biết còn nợ bao nhiêu và lộ trình trả nợ.</span>
              </div>
              <div>
                <b>Saving Spin</b>
                <span>Trò chơi nhỏ ghi nhận số tiền bạn tiết kiệm được.</span>
              </div>
            </div>
          </div>
        </section>

        {/* SECTION 06: GIAO DIỆN */}
        <section className="scene" id="s6">
          <div className="copy sty" id="sty" data-scrub data-style="0">
            <span className="eb">06 / Giao diện</span>
            <h2>
              Tài chính <em>đáng để ngắm</em>.
            </h2>
            <div className="sname" id="sname">Modern Dark</div>
            <div className="sw">
              <i className="on" />
              <i />
              <i />
            </div>
            <p>
              Tối giản hiện đại, Glassmorphism kính mờ hoặc Gradient năng động. Ở cả chế độ sáng và tối, độ tương phản chữ tuân theo WCAG AA.
            </p>
            <div className="note">
              Khung này minh họa từng phong cách khi bạn cuộn, không phải ảnh chụp ứng dụng.
            </div>
          </div>
        </section>

        {/* SECTION 07: ĐỘ TIN CẬY */}
        <section className="scene" id="s7">
          <div className="copy">
            <span className="eb">07 / Độ tin cậy</span>
            <h2>
              Mọi con số <em>truy nguyên được</em>.
            </h2>
            <p className="li">Báo cáo đẹp chỉ có giá trị khi con số đúng.</p>
            <div className="hlg">
              <div className="hl fx fx-wipe">
                <b>Số dư nguyên tử</b>
                <span>Firestore Transaction, bảo toàn kế toán kép.</span>
              </div>
              <div className="hl fx fx-wipe" style={{ transitionDelay: '.12s' }}>
                <b>Real-time Mobile &amp; Web</b>
                <span>Đồng bộ liền mạch giữa điện thoại và web.</span>
              </div>
              <div className="hl fx fx-wipe" style={{ transitionDelay: '.24s' }}>
                <b>Offline-first</b>
                <span>Dùng khi mất mạng, tự đồng bộ khi có lại.</span>
              </div>
              <div className="hl fx fx-wipe" style={{ transitionDelay: '.36s' }}>
                <b>Clean Architecture</b>
                <span>MVVM, Hilt, có kiểm thử cho UseCase và ViewModel.</span>
              </div>
            </div>
          </div>
        </section>

        {/* SECTION 08: BẮT ĐẦU */}
        <section className="scene" id="s8">
          <div className="copy">
            <span className="eb">08 / Bắt đầu</span>
            <h2>
              Hôm nay, <em>một quyết định nhỏ</em> cho tự do tài chính.
            </h2>
            <p className="li">Không còn những ngày cuối tháng bất an.</p>
            <ol className="steps">
              <li>
                <b>1. Đăng ký hoặc đăng nhập</b>
                Dùng tài khoản FinLux để mở không gian tài chính của bạn trên trình duyệt.
              </li>
              <li>
                <b>2. Thêm tài khoản và ghi giao dịch</b>
                Tạo ví, ghi nhận thu chi hoặc chuyển tiền giữa các tài khoản.
              </li>
              <li>
                <b>3. Theo dõi bức tranh tài chính</b>
                Xem lịch sử, ngân sách và mục tiêu đã đồng bộ trong cùng tài khoản.
              </li>
            </ol>
            <div className="btns">
              <Link
                href="/register"
                className="btn p"
              >
                Đăng ký tài khoản Web ngay →
              </Link>
              <Link
                href="/login"
                className="btn g"
              >
                Đã có tài khoản? Đăng nhập
              </Link>
            </div>
            <div className="note">
              Sử dụng trực tiếp trên trình duyệt, không cần cài công cụ lập trình.
            </div>
          </div>
        </section>
      </main>

      {/* FOOTER */}
      <footer>
        <span>© FinLux Project</span>
        <span style={{ display: 'inline-flex', gap: '16px', alignItems: 'center' }}>
          <Link
            href="/login"
            style={{ color: 'inherit', textDecoration: 'none' }}
          >
            Đăng nhập
          </Link>
          <Link
            href="/register"
            style={{ color: 'inherit', textDecoration: 'none' }}
          >
            Đăng ký
          </Link>
          <Link
            href="/terms"
            style={{ color: 'inherit', textDecoration: 'none', transition: 'color 0.2s' }}
          >
            Điều khoản &amp; Bảo mật
          </Link>
        </span>
        <span>Giấy phép MIT</span>
      </footer>
    </div>
  );
}
