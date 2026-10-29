/**
 * BASELINE RUNNER — 15 MONEY FLOWS & CANONICAL EDGE CASES AUDIT
 *
 * Kiểm tra đối soát 15 luồng tiền Hiến pháp và các ca kiểm thử biên giữa Android hiện hành và Web HEAD.
 * Chạy trực tiếp mã nguồn Web (semantics.ts, financeCalculations.ts).
 *
 * Phân định rõ 4 tầng kiểm thử độc lập:
 * 1. DOMAIN: Thực thi assertion trực tiếp trên các hàm domain (isLivingExpense, isDebt*, netWalletAssets, walletBalanceAt, budgetUsage, budgetWindow).
 * 2. USE_CASE: Kiểm tra tĩnh (Static Inspection) hành vi Use Case của Web (FinanceContext.tsx).
 * 3. INTEGRATION: Kiểm thử giao dịch Firestore / Rules / Listeners (15/15 UNVERIFIED vì chưa có Emulator, scope xử lý tại FW-033).
 * 4. SPEC: Kiểm tra tính tương thích đặc tả (ALIGNED hoặc SPEC_CONFLICT).
 */

import { loadModule } from './helpers/loadModule.mjs';
import { INITIAL_BASELINE, CANONICAL_15_FLOWS, CANONICAL_EDGE_CASES } from './fixtures/canonical15Flows.mjs';

const semantics = loadModule('src/lib/semantics.ts');
const calc = loadModule('src/lib/financeCalculations.ts');

export function runBaselineEvaluation() {
  let totalAssertions = 0;
  let passedAssertions = 0;
  let failedAssertions = 0;

  function assertEqual(actual, expected, description) {
    totalAssertions++;
    const pass = actual === expected;
    if (pass) {
      passedAssertions++;
    } else {
      failedAssertions++;
    }
    return { pass, actual, expected, description };
  }

  // Mô phỏng số dư ví theo công thức số học cập nhật cục bộ của Web (FinanceContext.tsx L744-758)
  function simulateWalletBalances(baseWallets, txOrPair) {
    const wallets = baseWallets.map((w) => ({ ...w }));
    const txList = Array.isArray(txOrPair) ? txOrPair : [txOrPair];
    for (const t of txList) {
      for (const w of wallets) {
        if (t.type === 'expense' && w.id === t.walletId) {
          w.balance -= t.amount;
        } else if (t.type === 'income' && w.id === t.walletId) {
          w.balance += t.amount;
        } else if (t.type === 'transfer_out') {
          if (w.id === t.walletId) w.balance -= t.amount;
          if (t.relatedWalletId && w.id === t.relatedWalletId && !txList.some((x) => x.type === 'transfer_in')) {
            w.balance += t.amount;
          }
        } else if (t.type === 'transfer_in') {
          if (w.id === t.walletId) w.balance += t.amount;
        }
      }
    }
    return wallets;
  }

  const baseWallets = INITIAL_BASELINE.wallets;
  const initialWebNetAssets = calc.netWalletAssets(baseWallets); // 55.000.000 đ

  const results = [];

  for (const flow of CANONICAL_15_FLOWS) {
    const flowAssertions = [];
    const tx = flow.tx || flow.txPair?.[0];

    // =========================================================================
    // 1. TẦNG DOMAIN: THỰC THI ASSERTION TRÊN MÃ NGUỒN DOMAIN THẬT CỦA WEB
    // =========================================================================
    let domainStatus = 'EXECUTED_PASS';
    let domainFindings = [];

    // Luồng 15: Web hoàn toàn chưa có mã xử lý CAPITAL_LOSS -> NOT_IMPLEMENTED ở tầng Domain, không gán EXECUTED_FAIL khi không có mã để chạy
    if (flow.flowNumber === 15) {
      domainStatus = 'NOT_IMPLEMENTED';
      domainFindings.push('Web chưa có định nghĩa DealFlowType.CAPITAL_LOSS hoặc hàm xử lý giảm vốn đầu tư');
    } else {
      // 1.1 Kiểm tra Semantics
      if (tx.type === 'expense') {
        const livingActual = semantics.isLivingExpense(tx);
        const livingAssert = assertEqual(
          livingActual,
          flow.businessExpected.isLivingExpense,
          `${flow.flowId}: isLivingExpense`
        );
        flowAssertions.push(livingAssert);
        if (!livingAssert.pass) {
          domainStatus = 'EXECUTED_FAIL';
          domainFindings.push(`semantics.isLivingExpense trả về ${livingActual} (kỳ vọng: ${flow.businessExpected.isLivingExpense})`);
        }

        if (flow.businessExpected.isDebtPrincipalSettlement !== undefined) {
          const principalActual = semantics.isDebtPrincipalSettlement(tx);
          const pAssert = assertEqual(
            principalActual,
            flow.businessExpected.isDebtPrincipalSettlement,
            `${flow.flowId}: isDebtPrincipalSettlement`
          );
          flowAssertions.push(pAssert);
          if (!pAssert.pass) {
            domainStatus = 'EXECUTED_FAIL';
            domainFindings.push(`semantics.isDebtPrincipalSettlement sai (${principalActual})`);
          }
        }

        if (flow.businessExpected.isDebtInterest !== undefined) {
          const interestActual = semantics.isDebtInterest(tx);
          const iAssert = assertEqual(
            interestActual,
            flow.businessExpected.isDebtInterest,
            `${flow.flowId}: isDebtInterest`
          );
          flowAssertions.push(iAssert);
          if (!iAssert.pass) {
            domainStatus = 'EXECUTED_FAIL';
            domainFindings.push(`semantics.isDebtInterest sai (${interestActual})`);
          }
        }
      } else if (tx.type === 'income') {
        const livingActual = semantics.isLivingExpense(tx);
        const livingAssert = assertEqual(
          livingActual,
          flow.businessExpected.isLivingExpense,
          `${flow.flowId}: income isLivingExpense must be false`
        );
        flowAssertions.push(livingAssert);
        if (!livingAssert.pass) {
          domainStatus = 'EXECUTED_FAIL';
          domainFindings.push(`income isLivingExpense trả về true`);
        }
      } else if (flow.txPair) {
        const livingActual = semantics.isLivingExpense(flow.txPair[0]);
        const livingAssert = assertEqual(
          livingActual,
          false,
          `${flow.flowId}: transfer isLivingExpense must be false`
        );
        flowAssertions.push(livingAssert);
        if (!livingAssert.pass) {
          domainStatus = 'EXECUTED_FAIL';
          domainFindings.push(`transfer isLivingExpense trả về true`);
        }

        const collapsed = semantics.collapseInternalTransferPairs(flow.txPair);
        const collapseAssert = assertEqual(
          collapsed.length,
          1,
          `${flow.flowId}: collapseInternalTransferPairs`
        );
        flowAssertions.push(collapseAssert);
      }

      // 1.2 Kiểm tra Net Worth Invariant thông qua hàm calc.netWalletAssets
      let simulatedNetWorthDelta = 0;
      if (flow.flowNumber === 3) {
        // FLOW_03: SIMULATED_RESULT dựa trên makeDebtPayment ghi expense đơn lẻ, không cộng ví thẻ
        const simulatedTxs = [{ type: 'expense', walletId: 'w_bank', amount: 2_000_000, categoryId: 'debt_payment' }];
        const walletsAfterSim = simulateWalletBalances(baseWallets, simulatedTxs);
        simulatedNetWorthDelta = calc.netWalletAssets(walletsAfterSim) - initialWebNetAssets; // -2.000.000 đ
        const nwAssert = assertEqual(
          simulatedNetWorthDelta,
          flow.businessExpected.netWorthDelta,
          `${flow.flowId}: Net Worth delta via simulated makeDebtPayment`
        );
        flowAssertions.push(nwAssert);
        if (!nwAssert.pass) {
          domainStatus = 'EXECUTED_FAIL';
          domainFindings.push(`SIMULATED_RESULT: Net Worth delta là -2.000.000 đ (kỳ vọng: 0 đ) do không cân bằng ví thẻ`);
        }
      } else if (flow.flowNumber === 11) {
        // FLOW_11: SIMULATED_RESULT dựa trên addSpinReward ghi type="income" trực tiếp
        const simulatedSpinTx = { type: 'income', walletId: 'w_cash', amount: 50_000, categoryId: null };
        const walletsAfterSim = simulateWalletBalances(baseWallets, simulatedSpinTx);
        simulatedNetWorthDelta = calc.netWalletAssets(walletsAfterSim) - initialWebNetAssets; // +50.000 đ
        const nwAssert = assertEqual(
          simulatedNetWorthDelta,
          flow.businessExpected.netWorthDelta,
          `${flow.flowId}: Net Worth delta via simulated addSpinReward`
        );
        flowAssertions.push(nwAssert);
        if (!nwAssert.pass) {
          domainStatus = 'EXECUTED_FAIL';
          domainFindings.push(`SIMULATED_RESULT: Net Worth delta tăng ảo +50.000 đ do ghi nhận type="income" thay vì trích quỹ`);
        }
      } else {
        const walletsAfter = simulateWalletBalances(baseWallets, flow.tx || flow.txPair);
        simulatedNetWorthDelta = calc.netWalletAssets(walletsAfter) - initialWebNetAssets;

        if (flow.isSpecConflict) {
          domainStatus = 'SPEC_CONFLICT';
          domainFindings.push(flow.specConflictDetail);
        } else {
          const nwAssert = assertEqual(
            simulatedNetWorthDelta,
            flow.businessExpected.netWorthDelta,
            `${flow.flowId}: netWalletAssets delta`
          );
          flowAssertions.push(nwAssert);
          if (!nwAssert.pass) {
            domainStatus = 'EXECUTED_FAIL';
            domainFindings.push(`SIMULATED_RESULT: Net Worth delta (${simulatedNetWorthDelta.toLocaleString()} đ) != chuẩn (${flow.businessExpected.netWorthDelta.toLocaleString()} đ)`);
          }
        }
      }

      // 1.3 Kiểm tra budgetUsage nếu flow có ngân sách tương ứng
      if (flow.businessExpected.budgetCategory) {
        const budgetList = INITIAL_BASELINE.budgets;
        const period = { start: '2026-10-01', end: '2026-10-31', name: 'Tháng này' };
        const txs = [flow.tx].filter(Boolean);
        const usage = calc.budgetUsage(budgetList, txs, period, INITIAL_BASELINE.timeZone);
        const catUsage = usage.find((u) => u.categoryId === flow.businessExpected.budgetCategory);
        if (catUsage) {
          const bAssert = assertEqual(
            catUsage.spent,
            flow.businessExpected.budgetSpentDelta,
            `${flow.flowId}: budgetUsage spent`
          );
          flowAssertions.push(bAssert);
          if (!bAssert.pass) {
            domainStatus = 'EXECUTED_FAIL';
            domainFindings.push(`budgetUsage tính spent = ${catUsage.spent} (kỳ vọng: ${flow.businessExpected.budgetSpentDelta})`);
          }
        }
      }
    }

    // =========================================================================
    // 2. TẦNG USE CASE: KIỂM TRA TĨNH (STATIC INSPECTION) MÃ NGUỒN WEB
    // =========================================================================
    let useCaseStatus = 'STATIC_INSPECTION';
    let useCaseFindings = '';

    if (flow.flowNumber === 2) {
      useCaseStatus = 'UNVERIFIED';
      useCaseFindings = 'Codebase Android & Web đều chỉ trừ số dư ví thẻ, không cập nhật DebtAccount.remainingBalance; rủi ro lệch pha bất đồng bộ.';
    } else if (flow.flowNumber === 3) {
      useCaseStatus = 'STATIC_INSPECTION';
      useCaseFindings = 'FinanceContext.tsx L969 makeDebtPayment() ghi type="expense", categoryId="debt_payment", không sinh cặp transfer, làm lệch số dư thẻ.';
    } else if (flow.flowNumber === 4) {
      useCaseStatus = 'STATIC_INSPECTION';
      useCaseFindings = 'FinanceContext.tsx L969-988 cập nhật addTransaction và debts riêng rẽ (non-atomic), tiềm ẩn nguy cơ lệch sổ cái khi có lỗi mạng.';
    } else if (flow.flowNumber === 10) {
      useCaseStatus = 'NOT_IMPLEMENTED';
      useCaseFindings = 'FinanceContext.tsx hoàn toàn chưa có Use Case withdrawFromGoal().';
    } else if (flow.flowNumber === 11) {
      useCaseStatus = 'STATIC_INSPECTION';
      useCaseFindings = 'FinanceContext.tsx L1078 addSpinReward() ghi trực tiếp type="income" vào ví mặc định thay vì hạch toán quỹ tiết kiệm.';
    } else if (flow.flowNumber === 12) {
      useCaseStatus = 'STATIC_INSPECTION';
      useCaseFindings = 'semantics.ts chưa kiểm tra DealFlowType.OUTLAY_CAPITAL, tính vốn đầu tư thành chi phí sinh hoạt.';
    } else if (flow.flowNumber === 13) {
      useCaseStatus = 'STATIC_INSPECTION';
      useCaseFindings = 'FinanceContext.tsx L540 periodIncome cộng toàn bộ income, làm 10M thu hồi vốn bị tính thành thu nhập phát sinh.';
    } else if (flow.flowNumber === 14) {
      useCaseStatus = 'NOT_IMPLEMENTED';
      useCaseFindings = 'Web chưa có thực thể Deal và Use Case quản trị danh mục đầu tư.';
    } else if (flow.flowNumber === 15) {
      useCaseStatus = 'NOT_IMPLEMENTED';
      useCaseFindings = 'Web chưa có thực thể Deal và Use Case ghi nhận CAPITAL_LOSS.';
    } else {
      useCaseStatus = 'STATIC_INSPECTION';
      useCaseFindings = 'Mã nguồn Web addTransaction() ghi nhận đúng loại giao dịch và cập nhật số dư ví.';
    }

    // =========================================================================
    // 3. TẦNG INTEGRATION: CHƯA CHẠY EMULATOR -> 100% UNVERIFIED
    // =========================================================================
    const integrationStatus = 'UNVERIFIED';

    // =========================================================================
    // 4. TẦNG SPEC: PHÂN ĐỊNH XUNG ĐỘT ĐẶC TẢ
    // =========================================================================
    const specStatus = flow.isSpecConflict ? 'SPEC_CONFLICT' : 'ALIGNED';

    // =========================================================================
    // 5. ĐÁNH GIÁ TỔNG QUAN (OVERALL AUDIT VERDICT)
    // =========================================================================
    let overallVerdict = 'DOMAIN_PASS';
    let summaryRootCause = '';

    if (specStatus === 'SPEC_CONFLICT') {
      overallVerdict = 'SPEC_CONFLICT';
      summaryRootCause = flow.specConflictDetail;
    } else if (domainStatus === 'NOT_IMPLEMENTED' || useCaseStatus === 'NOT_IMPLEMENTED') {
      overallVerdict = 'NOT_IMPLEMENTED';
      summaryRootCause = useCaseFindings || domainFindings.join('; ');
    } else if (domainStatus === 'EXECUTED_FAIL') {
      overallVerdict = 'DOMAIN_FAIL';
      summaryRootCause = domainFindings.join('; ');
    } else if (useCaseStatus === 'UNVERIFIED') {
      overallVerdict = 'STATIC_AUDIT_RISK';
      summaryRootCause = useCaseFindings;
    } else {
      overallVerdict = 'DOMAIN_PASS';
      summaryRootCause = 'Đạt 100% assertions Domain; Use Case đã rà soát tĩnh; Integration chưa kiểm chứng.';
    }

    results.push({
      flowId: flow.flowId,
      flowNumber: flow.flowNumber,
      name: flow.name,
      domainStatus,
      useCaseStatus,
      integrationStatus,
      specStatus,
      overallVerdict,
      summaryRootCause,
      taskFixId: flow.taskFixId,
      assertions: flowAssertions,
    });
  }

  // =========================================================================
  // 6. KIỂM THỬ MỞ RỘNG: CANONICAL EDGE CASES & BOUNDARY VERIFICATION
  // =========================================================================
  const edgeResults = [];

  // Edge 1: Backdate testing
  const backdate = CANONICAL_EDGE_CASES.backdate;
  const balBefore = calc.walletBalanceAt(backdate.wallet, [backdate.tx], backdate.asOfDates.beforeTxDate);
  const balAfter = calc.walletBalanceAt(backdate.wallet, [backdate.tx], backdate.asOfDates.afterTxDate);
  const a1 = assertEqual(balBefore, 10_000_000, 'Backdate: balance before tx date remains 10M');
  const a2 = assertEqual(balAfter, 8_000_000, 'Backdate: balance after tx date is reduced to 8M');
  edgeResults.push({ caseId: 'EDGE_BACKDATE', pass: a1.pass && a2.pass, description: backdate.description });

  // Edge 2: Salary cycle boundary testing
  const sal = CANONICAL_EDGE_CASES.salaryBoundary;
  const salWindow = calc.budgetWindow(sal.budget, sal.timeZone);
  const a3 = assertEqual(salWindow?.start, '2026-10-10', 'Salary Boundary: window starts on 2026-10-10');
  const a4 = assertEqual(salWindow?.end, '2026-11-09', 'Salary Boundary: window ends on 2026-11-09');
  const salPeriod = { start: '2026-10-01', end: '2026-11-30', name: 'Kỳ kiểm thử' };
  const salTxs = [sal.txInside, sal.txOutsideBefore, sal.txOutsideAfter];
  const salUsage = calc.budgetUsage([sal.budget], salTxs, salPeriod, sal.timeZone);
  const a5 = assertEqual(salUsage[0]?.spent, 1_000_000, 'Salary Boundary: budgetUsage filters only tx inside salary window');
  edgeResults.push({ caseId: 'EDGE_SALARY_BOUNDARY', pass: a3.pass && a4.pass && a5.pass, description: sal.description });

  // Edge 3: Legacy Category Compatibility testing
  const leg = CANONICAL_EDGE_CASES.legacyCompat;
  const a6 = assertEqual(semantics.isDebtPrincipalSettlement(leg.legacyPrincipalTx), true, 'Legacy: debt_principal recognized as principal');
  const a7 = assertEqual(semantics.isDebtInterest(leg.legacyInterestTx), true, 'Legacy: debt_interest recognized as interest');
  edgeResults.push({ caseId: 'EDGE_LEGACY_COMPAT', pass: a6.pass && a7.pass, description: leg.description });

  // Edge 4: Timezone midnight boundary testing
  const tz = CANONICAL_EDGE_CASES.timezoneBoundary;
  const parsedInstant = calc.financeDayInstant(tz.dayString, tz.timeZone);
  const formattedDay = calc.financeDate(parsedInstant, tz.timeZone);
  const a8 = assertEqual(formattedDay, tz.dayString, 'Timezone: financeDayInstant preserves local noon without date drift');
  edgeResults.push({ caseId: 'EDGE_TIMEZONE_MIDNIGHT', pass: a8.pass, description: tz.description });

  return {
    totalAssertions,
    passedAssertions,
    failedAssertions,
    results,
    edgeResults,
  };
}

export function printBaselineReport() {
  const { totalAssertions, passedAssertions, failedAssertions, results, edgeResults } = runBaselineEvaluation();

  console.log('='.repeat(130));
  console.log('FINLUX 15 MONEY FLOWS RIGOROUS BASELINE REPORT (GOLDEN BASELINE v2)');
  console.log('Thực thi assertion trực tiếp — Phân tách 4 cột: DOMAIN / USE_CASE / INTEGRATION / SPEC');
  console.log('='.repeat(130));
  console.log();

  // In bảng 15 luồng phân tách 4 cột kết quả
  console.log('| Flow ID | Luồng nghiệp vụ | DOMAIN | USE_CASE | INTEGRATION | SPEC | OVERALL | Root Cause / Sai lệch phát hiện | Task Fix |');
  console.log('|---|---|:---:|:---:|:---:|:---:|:---:|---|:---:|');

  for (const r of results) {
    console.log(
      `| ${r.flowId} | ${r.name} | ${r.domainStatus} | ${r.useCaseStatus} | ${r.integrationStatus} | ${r.specStatus} | **${r.overallVerdict}** | ${r.summaryRootCause} | ${r.taskFixId} |`
    );
  }

  console.log();
  console.log('='.repeat(130));
  console.log('KẾT QUẢ KIỂM THỬ MỞ RỘNG (CANONICAL EDGE CASES & BOUNDARIES):');
  for (const e of edgeResults) {
    console.log(`- [${e.pass ? 'PASS' : 'FAIL'}] ${e.caseId}: ${e.description}`);
  }

  console.log();
  console.log('='.repeat(130));
  console.log('TỔNG HỢP KẾT QUẢ THỰC THI ASSERTIONS & THỐNG KÊ CHI TIẾT THEO TỪNG TẦNG:');
  console.log(`- Tổng số Assertions thực thi:  ${totalAssertions}`);
  console.log(`- Assertions PASS:               ${passedAssertions} (${((passedAssertions / totalAssertions) * 100).toFixed(1)}%)`);
  console.log(`- Assertions FAIL:               ${failedAssertions} (${((failedAssertions / totalAssertions) * 100).toFixed(1)}%)`);
  console.log();

  console.log('1. TẦNG DOMAIN (Logic nghiệp vụ & Công thức kế toán):');
  const dPass = results.filter((r) => r.domainStatus === 'EXECUTED_PASS').length;
  const dFail = results.filter((r) => r.domainStatus === 'EXECUTED_FAIL').length;
  const dNotImpl = results.filter((r) => r.domainStatus === 'NOT_IMPLEMENTED').length;
  const dConflict = results.filter((r) => r.domainStatus === 'SPEC_CONFLICT').length;
  const dUnverified = results.filter((r) => r.domainStatus === 'UNVERIFIED').length;
  console.log(`   * EXECUTED_PASS:     ${dPass}/15 luồng`);
  console.log(`   * EXECUTED_FAIL:     ${dFail}/15 luồng`);
  console.log(`   * NOT_IMPLEMENTED:   ${dNotImpl}/15 luồng (FLOW_15)`);
  console.log(`   * SPEC_CONFLICT:     ${dConflict}/15 luồng (FLOW_09, FLOW_10)`);
  console.log(`   * UNVERIFIED:        ${dUnverified}/15 luồng`);
  console.log();

  console.log('2. TẦNG USE CASE (Rà soát tĩnh mã nguồn Web & Đối chiếu Android):');
  const uStatic = results.filter((r) => r.useCaseStatus === 'STATIC_INSPECTION').length;
  const uNotImpl = results.filter((r) => r.useCaseStatus === 'NOT_IMPLEMENTED').length;
  const uUnverified = results.filter((r) => r.useCaseStatus === 'UNVERIFIED').length;
  console.log(`   * STATIC_INSPECTION: ${uStatic}/15 luồng`);
  console.log(`   * NOT_IMPLEMENTED:   ${uNotImpl}/15 luồng (FLOW_10, FLOW_14, FLOW_15)`);
  console.log(`   * UNVERIFIED (RISK): ${uUnverified}/15 luồng (FLOW_02 - rủi ro lệch pha DebtAccount trên Android & Web)`);
  console.log();

  console.log('3. TẦNG INTEGRATION (Firestore Emulator & Backend Triggers):');
  const iUnverified = results.filter((r) => r.integrationStatus === 'UNVERIFIED').length;
  console.log(`   * UNVERIFIED:        ${iUnverified}/15 luồng (100% chưa kiểm chứng do chưa chạy Firebase Emulator; thuộc FW-033)`);
  console.log();

  console.log('4. TỔNG HỢP TOÀN DIỆN (OVERALL BASELINE VERDICT):');
  const ovDomainPass = results.filter((r) => r.overallVerdict === 'DOMAIN_PASS').length;
  const ovDomainFail = results.filter((r) => r.overallVerdict === 'DOMAIN_FAIL').length;
  const ovAuditRisk = results.filter((r) => r.overallVerdict === 'STATIC_AUDIT_RISK').length;
  const ovNotImpl = results.filter((r) => r.overallVerdict === 'NOT_IMPLEMENTED').length;
  const ovSpecConflict = results.filter((r) => r.overallVerdict === 'SPEC_CONFLICT').length;
  console.log(`   * DOMAIN_PASS:       ${ovDomainPass}/15 luồng (Domain đạt assertion, Use Case static audit sạch)`);
  console.log(`   * STATIC_AUDIT_RISK: ${ovAuditRisk}/15 luồng (FLOW_02: Số học khớp nhưng thiếu đồng bộ DebtAccount)`);
  console.log(`   * DOMAIN_FAIL:       ${ovDomainFail}/15 luồng (FLOW_03, FLOW_04, FLOW_11, FLOW_12, FLOW_13)`);
  console.log(`   * NOT_IMPLEMENTED:   ${ovNotImpl}/15 luồng (FLOW_14, FLOW_15)`);
  console.log(`   * SPEC_CONFLICT:     ${ovSpecConflict}/15 luồng (FLOW_09, FLOW_10)`);
  console.log('='.repeat(130));

  return { totalAssertions, passedAssertions, failedAssertions, results, edgeResults };
}

if (process.argv[1]?.endsWith('moneyFlows15.baseline.mjs')) {
  printBaselineReport();
}
