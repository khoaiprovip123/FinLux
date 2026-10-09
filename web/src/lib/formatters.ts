export function formatCurrency(amount: number): string {
  const isNegative = amount < 0;
  const abs = Math.abs(amount);
  const formatted = new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 0 }).format(Math.round(abs));
  return `${isNegative ? '-' : ''}${formatted} ₫`;
}

export function formatShortCurrency(amount: number): string {
  const abs = Math.abs(amount);
  const sign = amount < 0 ? '-' : '';
  if (abs >= 1_000_000_000) {
    return `${sign}${(abs / 1_000_000_000).toFixed(2).replace('.', ',')} tỷ`;
  }
  if (abs >= 1_000_000) {
    return `${sign}${(abs / 1_000_000).toFixed(1).replace('.', ',')} tr`;
  }
  return `${sign}${new Intl.NumberFormat('vi-VN').format(Math.round(abs))} ₫`;
}

export function formatPercent(ratio: number): string {
  return `${(ratio * 100).toFixed(1).replace('.', ',')}%`;
}

export function formatDate(dateString: string): string {
  try {
    const parts = dateString.slice(0, 10).split('-');
    if (parts.length === 3) {
      return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateString;
  } catch {
    return dateString;
  }
}

export function formatShortDate(dateString: string): string {
  try {
    const parts = dateString.slice(0, 10).split('-');
    if (parts.length === 3) {
      return `${parts[2]}/${parts[1]}`;
    }
    return dateString;
  } catch {
    return dateString;
  }
}
