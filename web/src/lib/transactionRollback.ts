import { doc, runTransaction, type Firestore } from 'firebase/firestore';
import { FIRESTORE_SCHEMA as schema } from '@/lib/firestoreSchema';

/** Read the complete rollback set before queuing any Firestore writes. */
export async function deleteTransactionWithRollback(db: Firestore, uid: string, id: string): Promise<void> {
  await runTransaction(db, async (transaction) => {
    const txRef = doc(db, schema.users, uid, schema.transactions, id);
    const original = await transaction.get(txRef);
    if (!original.exists()) return;
    const records = [original];
    const type = String(original.data().type).toLowerCase();
    if (type === 'transfer_out' || type === 'transfer_in') {
      const suffix = type === 'transfer_out' ? /_out$/ : /_in$/;
      if (suffix.test(id)) {
        const companionId = id.replace(suffix, type === 'transfer_out' ? '_in' : '_out');
        const companion = await transaction.get(doc(db, schema.users, uid, schema.transactions, companionId));
        if (!companion.exists()) throw new Error('Thiếu giao dịch chuyển khoản đối ứng; cần đối soát trước khi xóa.');
        const other = companion.data();
        const data = original.data();
        if (String(other.type).toLowerCase() !== (type === 'transfer_out' ? 'transfer_in' : 'transfer_out') ||
          other.amount !== data.amount || other.walletId !== data.relatedWalletId || other.relatedWalletId !== data.walletId) {
          throw new Error('Cặp giao dịch chuyển khoản không khớp.');
        }
        records.push(companion);
      } else if (type === 'transfer_in') {
        throw new Error('Không xác định được giao dịch chuyển khoản đối ứng.');
      }
    }

    const changes = new Map<string, number>();
    for (const record of records) {
      const data = record.data();
      const amount = Number(data.amount);
      const recordType = String(data.type).toLowerCase();
      if (!Number.isSafeInteger(amount) || amount <= 0 || !data.walletId) throw new Error('Giao dịch không hợp lệ.');
      const reversal = recordType === 'income' || recordType === 'transfer_in' ? -amount : amount;
      changes.set(data.walletId, (changes.get(data.walletId) || 0) + reversal);
      if (records.length === 1 && recordType === 'transfer_out' && data.relatedWalletId) {
        changes.set(data.relatedWalletId, (changes.get(data.relatedWalletId) || 0) - amount);
      }
    }
    const wallets = await Promise.all([...changes].map(async ([walletId, delta]) => {
      const ref = doc(db, schema.users, uid, schema.wallets, walletId);
      const snapshot = await transaction.get(ref);
      if (!snapshot.exists()) throw new Error('Không tìm thấy ví để hoàn trả số dư.');
      const balance = Number(snapshot.data()[schema.balance]) + delta;
      if (!Number.isSafeInteger(balance)) throw new Error('Số dư ví không hợp lệ.');
      return { ref, balance };
    }));
    for (const wallet of wallets) transaction.update(wallet.ref, { [schema.balance]: wallet.balance, [schema.lastTransactionId]: `rollback_${id}` });
    for (const record of records) transaction.delete(record.ref);
  });
}
