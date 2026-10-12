// Texture assets for LandingPage3D phone screen
// Procedural fallback textures generating high-res crisp app screens

export function createProceduralScreenTexture(type: 'home' | 'budget' | 'reports' | 'saving'): string {
  if (typeof document === 'undefined') return '';
  const canvas = document.createElement('canvas');
  canvas.width = 600;
  canvas.height = 1333;
  const ctx = canvas.getContext('2d');
  if (!ctx) return '';

  // Background gradient
  const bgGrad = ctx.createLinearGradient(0, 0, 0, 1333);
  bgGrad.addColorStop(0, '#0a1226');
  bgGrad.addColorStop(1, '#050a16');
  ctx.fillStyle = bgGrad;
  ctx.fillRect(0, 0, 600, 1333);

  // Status bar
  ctx.fillStyle = 'rgba(255, 255, 255, 0.7)';
  ctx.font = '600 20px "Space Mono", monospace';
  ctx.fillText('09:41', 40, 52);
  ctx.fillRect(520, 36, 40, 18);
  ctx.fillRect(490, 42, 20, 12);

  // Dynamic Island pill
  ctx.fillStyle = '#000000';
  ctx.beginPath();
  ctx.roundRect(220, 30, 160, 36, 18);
  ctx.fill();

  if (type === 'home') {
    // Header
    ctx.fillStyle = '#A7B1C7';
    ctx.font = '500 24px "Be Vietnam Pro", sans-serif';
    ctx.fillText('Xin chào, FinLux Member', 40, 130);

    ctx.fillStyle = '#F7F9FF';
    ctx.font = '800 42px "Bricolage Grotesque", sans-serif';
    ctx.fillText('Tổng tài sản ròng', 40, 185);

    // Glass Balance Card
    ctx.fillStyle = 'rgba(255, 255, 255, 0.08)';
    ctx.beginPath();
    ctx.roundRect(40, 220, 520, 240, 28);
    ctx.fill();
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.16)';
    ctx.lineWidth = 2;
    ctx.stroke();

    ctx.fillStyle = '#5BD7FF';
    ctx.font = '700 22px "Space Mono", monospace';
    ctx.fillText('DÒNG TIỀN TỰ DO (FCF)', 70, 275);

    ctx.fillStyle = '#F7F9FF';
    ctx.font = '800 52px "Bricolage Grotesque", sans-serif';
    ctx.fillText('+24.850.000 ₫', 70, 350);

    ctx.fillStyle = '#58D68D';
    ctx.font = '600 22px "Be Vietnam Pro", sans-serif';
    ctx.fillText('✦ Bảo toàn kế toán kép · Zero Double-Counting', 70, 410);

    // Wallet Pills Grid
    const wallets = [
      { name: 'Tiền mặt', color: '#58D68D', bal: '5.200.000 ₫' },
      { name: 'Ngân hàng', color: '#5BD7FF', bal: '42.500.000 ₫' },
      { name: 'Ví điện tử', color: '#7D7CFF', bal: '8.150.000 ₫' },
      { name: 'Đầu tư', color: '#FFD166', bal: '120.000.000 ₫' },
    ];

    wallets.forEach((w, i) => {
      const y = 490 + i * 110;
      ctx.fillStyle = 'rgba(255, 255, 255, 0.06)';
      ctx.beginPath();
      ctx.roundRect(40, y, 520, 92, 22);
      ctx.fill();
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.12)';
      ctx.stroke();

      ctx.fillStyle = w.color;
      ctx.beginPath();
      ctx.arc(80, y + 46, 12, 0, Math.PI * 2);
      ctx.fill();

      ctx.fillStyle = '#F7F9FF';
      ctx.font = '700 26px "Bricolage Grotesque", sans-serif';
      ctx.fillText(w.name, 110, y + 54);

      ctx.fillStyle = '#A7B1C7';
      ctx.font = '600 24px "Space Mono", monospace';
      ctx.textAlign = 'right';
      ctx.fillText(w.bal, 520, y + 54);
      ctx.textAlign = 'left';
    });

  } else if (type === 'budget') {
    // Budget cycle screen
    ctx.fillStyle = '#A7B1C7';
    ctx.font = '500 24px "Be Vietnam Pro", sans-serif';
    ctx.fillText('Chu kỳ lương: 05/10 — 04/11', 40, 130);

    ctx.fillStyle = '#F7F9FF';
    ctx.font = '800 42px "Bricolage Grotesque", sans-serif';
    ctx.fillText('Ngân sách chu kỳ', 40, 185);

    // Meter card
    ctx.fillStyle = 'rgba(255, 255, 255, 0.08)';
    ctx.beginPath();
    ctx.roundRect(40, 220, 520, 300, 28);
    ctx.fill();

    ctx.fillStyle = '#FFD166';
    ctx.font = '700 22px "Space Mono", monospace';
    ctx.fillText('TIẾN ĐỘ NGÂN SÁCH (82%)', 70, 275);

    ctx.fillStyle = '#F7F9FF';
    ctx.font = '800 48px "Bricolage Grotesque", sans-serif';
    ctx.fillText('16.400.000 / 20.000.000 ₫', 70, 345);

    // Progress bar
    ctx.fillStyle = 'rgba(255, 255, 255, 0.15)';
    ctx.beginPath();
    ctx.roundRect(70, 385, 460, 20, 10);
    ctx.fill();

    const barGrad = ctx.createLinearGradient(70, 0, 450, 0);
    barGrad.addColorStop(0, '#7D7CFF');
    barGrad.addColorStop(0.8, '#FFD166');
    barGrad.addColorStop(1, '#FF7E88');
    ctx.fillStyle = barGrad;
    ctx.beginPath();
    ctx.roundRect(70, 385, 380, 20, 10);
    ctx.fill();

    ctx.fillStyle = '#FFD166';
    ctx.font = '600 20px "Be Vietnam Pro", sans-serif';
    ctx.fillText('⚠ Cảnh báo: Đã đạt 80% hạn mức chu kỳ', 70, 455);

  } else if (type === 'reports') {
    // Reports screen
    ctx.fillStyle = '#A7B1C7';
    ctx.font = '500 24px "Be Vietnam Pro", sans-serif';
    ctx.fillText('Phân tích chi tiêu', 40, 130);

    ctx.fillStyle = '#F7F9FF';
    ctx.font = '800 42px "Bricolage Grotesque", sans-serif';
    ctx.fillText('Báo cáo dòng tiền', 40, 185);

    // Bar chart mock
    ctx.fillStyle = 'rgba(255, 255, 255, 0.08)';
    ctx.beginPath();
    ctx.roundRect(40, 220, 520, 400, 28);
    ctx.fill();

    const barH = [120, 180, 90, 240, 210, 290, 160];
    const days = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];
    barH.forEach((h, i) => {
      const bx = 75 + i * 65;
      const by = 550 - h;
      const bGrad = ctx.createLinearGradient(bx, by, bx, 550);
      bGrad.addColorStop(0, '#5BD7FF');
      bGrad.addColorStop(1, '#7D7CFF');
      ctx.fillStyle = bGrad;
      ctx.beginPath();
      ctx.roundRect(bx, by, 40, h, 8);
      ctx.fill();

      ctx.fillStyle = '#A7B1C7';
      ctx.font = '600 18px "Space Mono", monospace';
      ctx.fillText(days[i], bx + 8, 580);
    });

  } else {
    // Saving Spin mock
    ctx.fillStyle = '#FFD166';
    ctx.font = '700 24px "Space Mono", monospace';
    ctx.fillText('VÒNG QUAY MAY MẮN', 40, 130);

    ctx.fillStyle = '#F7F9FF';
    ctx.font = '800 42px "Bricolage Grotesque", sans-serif';
    ctx.fillText('Saving Spin', 40, 185);

    ctx.fillStyle = 'rgba(255, 255, 255, 0.08)';
    ctx.beginPath();
    ctx.roundRect(40, 220, 520, 480, 28);
    ctx.fill();

    ctx.fillStyle = '#5BD7FF';
    ctx.font = '600 24px "Be Vietnam Pro", sans-serif';
    ctx.fillText('Tiết kiệm mỗi ngày tạo thói quen lớn', 70, 280);

    // Mini wheel drawing
    ctx.save();
    ctx.translate(300, 460);
    const colors = ['#7D7CFF', '#5BD7FF', '#58D68D', '#FFD166', '#FF7E88'];
    for (let i = 0; i < 5; i++) {
      ctx.beginPath();
      ctx.moveTo(0, 0);
      ctx.arc(0, 0, 140, (i * Math.PI * 2) / 5, ((i + 1) * Math.PI * 2) / 5);
      ctx.fillStyle = colors[i];
      ctx.fill();
    }
    ctx.beginPath();
    ctx.arc(0, 0, 40, 0, Math.PI * 2);
    ctx.fillStyle = '#10182E';
    ctx.fill();
    ctx.restore();
  }

  return canvas.toDataURL('image/png');
}
