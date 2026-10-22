import test from 'node:test';
import assert from 'node:assert/strict';
import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { loadModule } from './helpers/loadModule.mjs';

const calc = loadModule('src/lib/financeCalculations.ts');
const zone = calc.FINANCE_CONFIG.defaultTimeZone;
const period = (start, end) => ({ start, end, name: 'test' });
const wallet = (id, balance, type = 'cash') => ({ id, balance, type });
const tx = (id, type, walletId, amount, date, extra = {}) => ({ id, type, walletId, amount, date, ...extra });
const budget = (month, limitAmount = 100) => ({ id: `food_month:${month}`, categoryId: 'food', month, periodKey: `month:${month}`, limitAmount, spentAmount: 0 });

test('current periods include transactions after the former fixed cutoff', () => {
  const periods = calc.reportPeriods('2026-10-21');
  assert.equal(periods.month.end, '2026-10-21');
  assert.equal(periods.six.start, '2026-05-01');
});
test('rolling periods cross years and handle leap February', () => {
  const periods = calc.reportPeriods('2024-03-01');
  assert.equal(periods.previous.end, '2024-02-29');
  assert.equal(periods.six.start, '2023-10-01');
  assert.equal(calc.reportPeriods('2027-01-02').previous.start, '2026-12-01');
});
test('all includes records from earlier years and future recorded dates', () => {
  const all = calc.reportPeriods('2026-10-21', ['2020-01-01', '2026-11-01']).all;
  assert.equal(all.start, '2020-01-01');
  assert.equal(all.end, '2026-11-01');
});
test('empty periods still have a valid range', () => {
  const all = calc.reportPeriods('2026-10-21').all;
  assert.equal(all.start, all.end);
});
test('prerender has no fabricated current date or invalid chart months', () => {
  const initial = calc.reportPeriods('');
  assert.equal(initial.month.start, '');
  assert.equal(calc.monthsInPeriod(initial.month).length, 0);
});
test('financial day uses the configured timezone near midnight', () => {
  assert.equal(calc.financeDate(new Date('2026-10-20T18:00:00Z'), zone), '2026-10-21');
  assert.equal(calc.financeDate(new Date('2026-10-20T18:00:00Z'), 'America/New_York'), '2026-10-20');
  assert.equal(calc.validFinanceTimeZone('invalid'), zone);
});
test('saving a selected date uses finance timezone rather than browser timezone', () => {
  assert.equal(calc.financeDayInstant('2026-10-21', zone).toISOString(), '2026-10-21T05:00:00.000Z');
  assert.equal(calc.financeDayInstant('2026-07-01', 'America/New_York').toISOString(), '2026-07-01T16:00:00.000Z');
  assert.equal(calc.financeDayInstant('2026-01-01', 'America/New_York').toISOString(), '2026-01-01T17:00:00.000Z');
  assert.throws(() => calc.financeDayInstant('2026-02-31', zone), /không hợp lệ/);
});
test('chart months cross December/January without a fixed year', () => {
  assert.equal(calc.monthsInPeriod(period('2026-12-12', '2027-02-02')).join(','), '2026-12,2027-01,2027-02');
});
test('current wallet balance does not replay already included income', () => {
  assert.equal(calc.walletBalanceAt(wallet('cash', 100), [tx('i', 'income', 'cash', 20, '2026-10-01')], '2026-10-21'), 100);
});
test('historical wallet balance reverses only later entries', () => {
  const entries = [tx('i', 'income', 'cash', 30, '2026-10-10'), tx('e', 'expense', 'cash', 10, '2026-10-15')];
  assert.equal(calc.walletBalanceAt(wallet('cash', 120), entries, '2026-10-09'), 100);
  assert.equal(calc.walletBalanceAt(wallet('cash', 120), entries, '2026-10-10'), 130);
});
test('paired transfers reverse each wallet once', () => {
  const entries = [tx('t_out', 'transfer_out', 'a', 20, '2026-10-10', { relatedWalletId: 'b' }), tx('t_in', 'transfer_in', 'b', 20, '2026-10-10', { relatedWalletId: 'a' })];
  assert.equal(calc.walletBalanceAt(wallet('a', 80), entries, '2026-10-09'), 100);
  assert.equal(calc.walletBalanceAt(wallet('b', 70), entries, '2026-10-09'), 50);
});
test('legacy single-entry transfer still reverses the receiving wallet', () => {
  assert.equal(calc.walletBalanceAt(wallet('b', 70), [tx('legacy', 'transfer_out', 'a', 20, '2026-10-10', { relatedWalletId: 'b' })], '2026-10-09'), 50);
});
test('credit liabilities reduce net wallet assets', () => {
  assert.equal(calc.netWalletAssets([wallet('a', 100), wallet('c', -20, 'card')]), 80);
});
test('monthly budgets use only their original window', () => {
  const entries = [tx('a', 'expense', 'cash', 20, '2026-09-15', { categoryId: 'food' }), tx('b', 'expense', 'cash', 30, '2026-10-15', { categoryId: 'food' })];
  const usage = calc.budgetUsage([budget('2026-09'), budget('2026-10')], entries, period('2026-09-01', '2026-10-21'), zone);
  assert.equal(usage.reduce((sum, row) => sum + row.spent, 0), 50);
  assert.equal(usage.reduce((sum, row) => sum + row.limit, 0), 200);
  assert.equal(calc.budgetUsage([budget('2026-09'), budget('2026-10')], entries, period('2026-10-01', '2026-10-21'), zone).length, 1);
});
test('partial report does not count entries outside the filter', () => {
  const usage = calc.budgetUsage([budget('2026-10')], [tx('a', 'expense', 'cash', 20, '2026-10-05', { categoryId: 'food' })], period('2026-10-10', '2026-10-21'), zone);
  assert.equal(usage[0].spent, 0);
  assert.equal(usage[0].limit, 100);
});
test('legacy/canonical budget documents do not duplicate spending', () => {
  const legacy = { ...budget('2026-10'), id: 'food_2026-10', periodKey: undefined, limitAmount: 200 };
  const usage = calc.budgetUsage([legacy, budget('2026-10')], [], period('2026-10-01', '2026-10-21'), zone);
  assert.equal(usage.length, 1);
  assert.equal(usage[0].limit, 100);
});
test('salary budget prefers stored epoch boundaries and excludes its end', () => {
  const salary = { ...budget(''), periodKey: 'salary:2026-09-25', periodStart: Date.parse('2026-09-24T17:00:00Z'), periodEndExclusive: Date.parse('2026-10-24T17:00:00Z') };
  const window = calc.budgetWindow(salary, zone);
  assert.equal(window.start, '2026-09-25');
  assert.equal(window.end, '2026-10-24');
  const entries = [tx('a', 'expense', 'cash', 10, '2026-10-24', { categoryId: 'food' }), tx('b', 'expense', 'cash', 20, '2026-10-25', { categoryId: 'food' })];
  assert.equal(calc.budgetUsage([salary], entries, period('2026-10-01', '2026-10-31'), zone)[0].spent, 10);
});
test('legacy salary budget resolves month end without date overflow', () => {
  assert.equal(calc.budgetWindow({ ...budget(''), periodKey: 'salary:2026-01-31' }, zone).end, '2026-02-27');
});
test('unknown budget windows are excluded instead of using a fixed date', () => {
  assert.equal(calc.budgetWindow({ ...budget(''), id: 'unknown', periodKey: undefined }, zone), null);
});
test('invalid budget dates are skipped without crashing financial views', () => {
  assert.equal(calc.budgetWindow(budget('2026-99'), zone), null);
  assert.equal(calc.budgetWindow({ ...budget(''), periodKey: 'salary:2026-02-31' }, zone), null);
});

function renderGoals(goals, wallets = []) {
  const view = loadModule('src/components/views/GoalsView.tsx', {
    '@/context/FinanceContext': { useFinance: () => ({ goals, wallets }) },
    '@/components/icons/FinluxIcons': { FinluxIcon: () => null },
  }).default;
  return renderToStaticMarkup(React.createElement(view));
}
test('empty goal screen shows an empty state instead of a fabricated target', () => {
  const html = renderGoals([], [wallet('cash', 500_000)]);
  assert.match(html, /Chưa có mục tiêu tài chính/);
  assert.doesNotMatch(html, /180\.000\.000|Quỹ tự do tài chính/);
});
test('goal progress uses goal savings rather than a larger unrelated wallet balance', () => {
  const html = renderGoals([{ id: 'goal', name: 'Laptop', targetAmount: 1_000_000, currentAmount: 500_000 }], [wallet('cash', 900_000)]);
  assert.match(html, /500\.000 ₫/);
  assert.match(html, /50,0%/);
  assert.doesNotMatch(html, /900\.000/);
});

function transactionFormFixture(saveResult) {
  const state = [];
  const writes = [];
  let cursor = 0;
  let closed = false;
  const view = loadModule('src/components/TransactionModal.tsx', {
    react: { ...React, useState: (initial) => {
      const index = cursor++;
      if (!(index in state)) state[index] = initial;
      return [state[index], (value) => { state[index] = value; }];
    } },
    '@/context/FinanceContext': { useFinance: () => ({
      wallets: [wallet('actual-wallet-id', 1000)],
      categories: [{ id: 'custom-food', type: 'expense', name: 'Food' }], financeTimeZone: zone,
      addTransaction: async (entry) => { writes.push(entry); return saveResult; }, showToast: () => {},
    }) },
    '@/components/icons/FinluxIcons': { FinluxIcon: () => null },
  }).default;
  const render = () => { cursor = 0; return view({ isOpen: true, onClose: () => { closed = true; } }); };
  const find = (node, predicate) => {
    if (!React.isValidElement(node)) return null;
    if (predicate(node)) return node;
    for (const child of React.Children.toArray(node.props.children)) {
      const match = find(child, predicate);
      if (match) return match;
    }
    return null;
  };
  const field = (id) => find(render(), (node) => node.props.id === id);
  return {
    field, writes, closed: () => closed,
    enter: (id, value) => field(id).props.onChange({ target: { value } }),
    submit: () => find(render(), (node) => node.type === 'form').props.onSubmit({ preventDefault() {} }),
  };
}
test('failed transaction save preserves input and keeps the form open', async () => {
  const form = transactionFormFixture(false);
  form.enter('fx-modal-amount', '250');
  form.enter('fx-modal-note', 'Lunch');
  await form.submit();
  assert.equal(form.closed(), false);
  assert.equal(form.field('fx-modal-note').props.value, 'Lunch');
  assert.equal(form.field('fx-modal-amount').props.value, '250');
});
test('successful transaction uses real wallet/category IDs and resets only after save', async () => {
  const form = transactionFormFixture(true);
  form.enter('fx-modal-amount', '250');
  form.enter('fx-modal-note', 'Lunch');
  await form.submit();
  assert.equal(form.writes[0].walletId, 'actual-wallet-id');
  assert.equal(form.writes[0].categoryId, 'custom-food');
  assert.equal(form.writes[0].date, calc.financeDate(new Date(), zone));
  assert.equal(form.closed(), true);
  assert.equal(form.field('fx-modal-note').props.value, '');
});
test('invalid amount never reaches the transaction writer', async () => {
  const form = transactionFormFixture(true);
  form.enter('fx-modal-amount', '-10');
  form.enter('fx-modal-note', 'Invalid');
  await form.submit();
  assert.equal(form.writes.length, 0);
  assert.equal(form.closed(), false);
});

function rollbackFixture(overrides = {}) {
  const data = {
    'transactions/t_out': { type: 'transfer_out', amount: 20, walletId: 'a', relatedWalletId: 'b' },
    'transactions/t_in': { type: 'transfer_in', amount: 20, walletId: 'b', relatedWalletId: 'a' },
    'wallets/a': { balance: 80 }, 'wallets/b': { balance: 70 }, ...overrides,
  };
  const operations = [];
  let writesStarted = false;
  const sdk = {
    doc: (_db, _users, _uid, collection, id) => `${collection}/${id}`,
    runTransaction: async (_db, callback) => callback({
      get: async (ref) => {
        assert.equal(writesStarted, false, 'all reads must precede all writes');
        return { ref, exists: () => data[ref] != null, data: () => data[ref] };
      },
      update: (ref, values) => { writesStarted = true; operations.push({ ref, values }); },
      delete: (ref) => { writesStarted = true; operations.push({ deleted: ref }); },
    }),
  };
  const { deleteTransactionWithRollback } = loadModule('src/lib/transactionRollback.ts', { 'firebase/firestore': sdk });
  return { remove: (id) => deleteTransactionWithRollback({}, 'test-user', id), operations };
}
for (const id of ['t_out', 't_in']) {
  test(`deleting ${id} reads everything first, restores both wallets and deletes both halves`, async () => {
    const fixture = rollbackFixture();
    await fixture.remove(id);
    assert.equal(fixture.operations.find((op) => op.ref === 'wallets/a').values.balance, 100);
    assert.equal(fixture.operations.find((op) => op.ref === 'wallets/b').values.balance, 50);
    assert.equal(fixture.operations.filter((op) => op.deleted).length, 2);
  });
}
test('missing companion aborts without changing a wallet', async () => {
  const fixture = rollbackFixture({ 'transactions/t_in': null });
  await assert.rejects(fixture.remove('t_out'), /đối ứng/);
  assert.equal(fixture.operations.length, 0);
});
test('missing wallet aborts without partial writes', async () => {
  const fixture = rollbackFixture({ 'wallets/b': null });
  await assert.rejects(fixture.remove('t_out'), /Không tìm thấy ví/);
  assert.equal(fixture.operations.length, 0);
});
test('mismatched companion aborts without writes', async () => {
  const fixture = rollbackFixture({ 'transactions/t_in': { type: 'transfer_in', amount: 50, walletId: 'b', relatedWalletId: 'a' } });
  await assert.rejects(fixture.remove('t_out'), /không khớp/);
  assert.equal(fixture.operations.length, 0);
});
test('already deleted transaction is a no-op', async () => {
  const fixture = rollbackFixture({ 'transactions/t_out': null });
  await fixture.remove('t_out');
  assert.equal(fixture.operations.length, 0);
});
test('expense deletion also reverses uppercase Android transaction types', async () => {
  const fixture = rollbackFixture({ 'transactions/e': { type: 'EXPENSE', amount: 20, walletId: 'a' } });
  await fixture.remove('e');
  assert.equal(fixture.operations[0].values.balance, 100);
});
test('legacy transfer deletion restores both wallets', async () => {
  const fixture = rollbackFixture({ 'transactions/legacy': { type: 'transfer_out', amount: 20, walletId: 'a', relatedWalletId: 'b' } });
  await fixture.remove('legacy');
  assert.equal(fixture.operations.find((op) => op.ref === 'wallets/b').values.balance, 50);
});
