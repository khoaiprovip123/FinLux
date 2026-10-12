'use client';

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useFinance } from '@/context/FinanceContext';
import * as THREE from 'three';
import { createProceduralScreenTexture } from './landingScreens';

export default function LandingPage3D() {
  const router = useRouter();
  const { theme, toggleTheme } = useFinance();
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [noGl, setNoGl] = useState(false);
  const [activeScene, setActiveScene] = useState(0);
  const [copiedCode, setCopiedCode] = useState(false);

  const scrollTo = useCallback((id: string) => {
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
  }, []);

  const copyToClipboard = (text: string) => {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text);
      setCopiedCode(true);
      setTimeout(() => setCopiedCode(false), 2000);
    }
  };

  useEffect(() => {
    const cv = canvasRef.current;
    if (!cv) return;

    let renderer: THREE.WebGLRenderer;
    try {
      renderer = new THREE.WebGLRenderer({
        canvas: cv,
        alpha: true,
        antialias: true,
        powerPreference: 'high-performance',
      });
    } catch {
      setNoGl(true);
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

    // Glowing orbital ring around phone
    const ring = new THREE.Mesh(new THREE.TorusGeometry(2.6, 0.05, 16, 120), gm(0x7d7cff, 0.5));
    ring.rotation.x = 1.2;
    gP.add(ring);

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
        r: 2.9 + Math.random() * 1.4,
        t: Math.random() * 6.28,
        sp: 0.15 + Math.random() * 0.3,
        y: (Math.random() - 0.5) * 2.8,
      };
      scene.add(o);
      orbs.push(o);
    }

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
      W = window.innerWidth;
      H = window.innerHeight;
      renderer.setSize(W, H, false);
      cam.aspect = W / H;
      cam.updateProjectionMatrix();
    }
    window.addEventListener('resize', size);
    size();

    const onPointerMove = (e: MouseEvent) => {
      px = e.clientX / W - 0.5;
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
        const p = sec.classList.contains('tall')
          ? cl(-b.top / (b.height - vh))
          : cl((vh * 0.75 - b.top) / (b.height * 0.75));
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

      const vh = window.innerHeight;
      sc.forEach((s, i) => {
        s.classList.toggle('cur', i === b);
        const q = s.getBoundingClientRect();
        s.style.setProperty(
          '--gy',
          s.classList.contains('tall')
            ? '0'
            : ((q.top + q.height / 2 - vh / 2) * -0.3).toFixed(1)
        );
      });
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
    const clock = new THREE.Clock();
    function L(v: number, target: number, k: number) {
      return v + (target - v) * k;
    }

    let animId: number;
    const pv = new THREE.Vector3();

    function tick() {
      animId = requestAnimationFrame(tick);
      if (document.hidden) return;

      const t = clock.getElapsedTime();
      const wide = W > 900;
      const idx = currentSceneIdx;
      const actGroup = gs[idx];

      const gx = wide ? 2.2 : 0;
      const gy = wide ? 0 : 1.8;
      const scScale = wide ? 1 : 0.6;

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
      ring.rotation.z = reduce ? 0 : t * 0.2;
      if (!reduce) wheel.rotation.z = -t * 0.7;

      // Orbiting spheres
      orbs.forEach((o) => {
        const d = o.userData as { r: number; t: number; sp: number; y: number };
        if (!reduce) d.t += d.sp * 0.01;
        o.position.set(
          Math.cos(d.t) * d.r + root.position.x,
          d.y + Math.sin(t * 0.6 + d.r) * 0.15 + root.position.y,
          Math.sin(d.t) * d.r * 0.5
        );
      });

      // 3D Orbital Chips projected to 2D Screen Coordinates
      const oc = Array.from(document.querySelectorAll<HTMLElement>('.ochip'));
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
        el.style.transform = `translate(${sx.toFixed(1)}px,${sy.toFixed(1)}px) translate(-50%,-50%) scale(${k.toFixed(3)})`;
        el.style.opacity = show ? (z > -0.4 ? '1' : '0.45') : '0';
      });

      renderer.render(scene, cam);
    }

    tick();

    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener('resize', size);
      window.removeEventListener('scroll', act);
      window.removeEventListener('pointermove', onPointerMove);
      cv.removeEventListener('pointerdown', onPointerDown);
      window.removeEventListener('pointerup', onPointerUp);
      io.disconnect();
      renderer.dispose();
      scene.clear();
    };
  }, []);

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
            className={`dock-link ${activeScene === 1 ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              scrollTo('s1');
            }}
          >
            Đa ví
          </a>
          <a
            href="#s2"
            className={`dock-link ${activeScene === 2 ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              scrollTo('s2');
            }}
          >
            Dòng tiền FCF
          </a>
          <a
            href="#s3"
            className={`dock-link ${activeScene === 3 ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              scrollTo('s3');
            }}
          >
            Chu kỳ lương
          </a>
          <a
            href="#s4"
            className={`dock-link ${activeScene === 4 ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              scrollTo('s4');
            }}
          >
            Báo cáo
          </a>
          <a
            href="#s5"
            className={`dock-link ${activeScene === 5 ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              scrollTo('s5');
            }}
          >
            Tiết kiệm
          </a>
          <a
            href="#s6"
            className={`dock-link ${activeScene === 6 ? 'active' : ''}`}
            onClick={(e) => {
              e.preventDefault();
              scrollTo('s6');
            }}
          >
            Giao diện
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

          <a
            className="btn-dock-cta"
            href="#s8"
            onClick={(e) => {
              e.preventDefault();
              scrollTo('s8');
            }}
          >
            <span>Trải nghiệm ngay</span>
            <span className="cta-arrow">→</span>
          </a>
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
              <a
                className="btn p"
                href="#s8"
                onClick={(e) => {
                  e.preventDefault();
                  scrollTo('s8');
                }}
              >
                Bắt đầu quản trị tài chính →
              </a>
              <a
                className="btn g"
                href="#s1"
                onClick={(e) => {
                  e.preventDefault();
                  scrollTo('s1');
                }}
              >
                Khám phá tính năng ↓
              </a>
            </div>
          </div>
        </section>

        {/* SECTION 01: ĐA VÍ */}
        <section className="scene" id="s1">
          <span className="ghost" aria-hidden="true">01</span>
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
          <span className="ghost" aria-hidden="true">02</span>
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
          <span className="ghost" aria-hidden="true">03</span>
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
          <span className="ghost" aria-hidden="true">04</span>
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
          <span className="ghost" aria-hidden="true">05</span>
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

        {/* SECTION 06: GIAO DIỆN TALL SCENE */}
        <section className="scene tall" id="s6">
          <div className="stick">
            <span className="ghost" aria-hidden="true">06</span>
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
          </div>
        </section>

        {/* SECTION 07: ĐỘ TIN CẬY */}
        <section className="scene" id="s7">
          <span className="ghost" aria-hidden="true">07</span>
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
          <span className="ghost" aria-hidden="true">08</span>
          <div className="copy">
            <span className="eb">08 / Bắt đầu</span>
            <h2>
              Hôm nay, <em>một quyết định nhỏ</em> cho tự do tài chính.
            </h2>
            <p className="li">Không còn những ngày cuối tháng bất an.</p>
            <ol className="steps">
              <li>
                <b>1. Lấy mã nguồn</b>
                <code>git clone https://github.com/khoaiprovip123/FinLux.git</code>
                <br />
                <button
                  className="btn g s cp"
                  id="cp"
                  type="button"
                  onClick={() => copyToClipboard('git clone https://github.com/khoaiprovip123/FinLux.git')}
                >
                  {copiedCode ? '✓ Đã sao chép' : 'Sao chép lệnh'}
                </button>
              </li>
              <li>
                <b>2. Chạy bản debug</b>
                Mở bằng Android Studio và nhấn Run. Ứng dụng tự dùng dữ liệu mẫu, chưa cần Firebase.
              </li>
              <li>
                <b>3. Kết nối Firebase của bạn (tùy chọn)</b>
                Thêm file google-services.json vào thư mục app.
              </li>
            </ol>
            <div className="btns">
              <button
                type="button"
                className="btn p"
                onClick={() => router.push('/login')}
              >
                Bắt đầu quản trị tài chính →
              </button>
              <a
                className="btn g"
                href="https://github.com/khoaiprovip123/FinLux#readme"
                target="_blank"
                rel="noreferrer"
              >
                Xem hướng dẫn đầy đủ ↗
              </a>
            </div>
            <div className="note">
              Cần Android Studio, JDK 17 và thiết bị hoặc máy ảo Android 8.0 trở lên.
            </div>
          </div>
        </section>
      </main>

      {/* FOOTER */}
      <footer>
        <span>© FinLux Project</span>
        <span>Giấy phép MIT</span>
      </footer>
    </div>
  );
}
