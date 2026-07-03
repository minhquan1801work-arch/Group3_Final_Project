/**
 * seed_test_accounts.js — Tao 5 account test + du lieu day du de test app
 *
 * Tao cho moi user: Auth account, customers doc, gio hang (carts/cartDetails),
 * don hang (orders/orderDetails), payments, favorites, notifications.
 *
 * CACH DUNG:
 *   npm install firebase-admin
 *   node seed_test_accounts.js
 *
 * TAT CA ACCOUNT DUNG PASSWORD: Test@123
 */

const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const { getAuth } = require('firebase-admin/auth');
const serviceAccount = require('./service-account.json');

initializeApp({ credential: cert(serviceAccount), projectId: 'glassity-770c5' });

const db = getFirestore();
const auth = getAuth();

const PASSWORD = 'Test@123';

const USERS = [
  { email: 'test1@glassity.com', name: 'Nguyễn Văn An',   phone: '0901234561', address: '12 Nguyễn Huệ, Q.1, TP.HCM' },
  { email: 'test2@glassity.com', name: 'Trần Thị Bích',   phone: '0901234562', address: '45 Lê Lợi, Q.3, TP.HCM' },
  { email: 'test3@glassity.com', name: 'Lê Minh Cường',   phone: '0901234563', address: '78 Hai Bà Trưng, Q.1, TP.HCM' },
  { email: 'test4@glassity.com', name: 'Phạm Thu Dung',   phone: '0901234564', address: '23 Trần Hưng Đạo, Q.5, TP.HCM' },
  { email: 'test5@glassity.com', name: 'Hoàng Quốc Em',   phone: '0901234565', address: '90 Võ Văn Tần, Q.3, TP.HCM' },
];

// Ngay trong qua khu (n ngay truoc)
const daysAgo = (n) => new Date(Date.now() - n * 24 * 60 * 60 * 1000);

async function ensureUser(u) {
  try {
    const existing = await auth.getUserByEmail(u.email);
    await auth.updateUser(existing.uid, { password: PASSWORD, displayName: u.name });
    return existing.uid;
  } catch (e) {
    const created = await auth.createUser({ email: u.email, password: PASSWORD, displayName: u.name });
    return created.uid;
  }
}

async function seedCustomer(uid, u, createdDaysAgo) {
  // LUU Y: KHONG ghi field customerId vao doc — model dung @DocumentId,
  // neu field id ton tai trong document thi toObject() se crash!
  await db.collection('customers').doc(uid).set({
    name: u.name,
    email: u.email,
    phone: u.phone,
    address: u.address,
    loginProvider: 'email',
    createdAt: daysAgo(createdDaysAgo),
  });
}

// cartId == customerId (theo CartRepository)
async function seedCart(uid, items) {
  await db.collection('carts').doc(uid).set({ customerId: uid, createdAt: new Date() });
  for (const it of items) {
    const ref = db.collection('carts').doc(uid).collection('cartDetails').doc();
    await ref.set({ productId: it.productId, quantity: it.quantity, color: it.color });
  }
}

let orderSeq = 1;
async function seedOrder(uid, opts) {
  const { items, paymentMethod, paymentStatus, orderStatus, address, createdDaysAgo } = opts;
  const total = items.reduce((s, it) => s + it.price * it.quantity, 0);
  const ref = db.collection('orders').doc();
  const orderCode = 'GLS-2026-' + String(orderSeq++).padStart(4, '0');
  await ref.set({
    customerId: uid,
    orderCode,
    totalAmount: total,
    paymentMethod,
    paymentStatus,
    orderStatus,
    shippingAddress: address,
    createdAt: daysAgo(createdDaysAgo),
  });
  for (const it of items) {
    const d = ref.collection('orderDetails').doc();
    await d.set({ productId: it.productId, quantity: it.quantity, price: it.price, color: it.color });
  }
  // Payment tuong ung
  const p = db.collection('payments').doc();
  await p.set({
    orderId: ref.id,
    customerId: uid,
    method: paymentMethod,
    status: paymentStatus === 'PAID' ? 'SUCCESS' : (orderStatus === 'CANCELLED' ? 'FAILED' : 'PENDING'),
    transactionId: paymentMethod === 'BANK_TRANSFER' && paymentStatus === 'PAID' ? 'TXN' + Date.now() + orderSeq : null,
    amount: total,
    createdAt: daysAgo(createdDaysAgo),
  });
  return orderCode;
}

async function seedFavorites(uid, productIds) {
  for (const pid of productIds) {
    const ref = db.collection('favorites').doc();
    await ref.set({ customerId: uid, productId: pid, createdAt: new Date() });
  }
}

async function seedNotifications(uid, notis) {
  for (const n of notis) {
    const ref = db.collection('notifications').doc();
    await ref.set({ customerId: uid, message: n.message, status: n.status, type: n.type, createdAt: daysAgo(n.d) });
  }
}

async function main() {
  console.log('=== Glassity Test Accounts Seeder ===\n');

  // Lay san pham that tu Firestore
  const snap = await db.collection('products').get();
  const products = snap.docs.map(d => ({ id: d.id, ...d.data() }));
  if (products.length < 8) throw new Error('Chua du san pham trong Firestore — chay seed_firestore.js truoc');
  console.log(`Tim thay ${products.length} san pham.`);
  const P = (i) => ({ productId: products[i].id, price: products[i].price, color: (products[i].colors || ['#1A1614'])[0] });

  // Xoa data test cu (orders/payments/favorites/notifications cua test users)
  const uids = [];
  for (const u of USERS) uids.push(await ensureUser(u));
  console.log('Auth users OK:', uids.length);

  for (const col of ['orders', 'payments', 'favorites', 'notifications']) {
    for (const uid of uids) {
      const old = await db.collection(col).where('customerId', '==', uid).get();
      for (const doc of old.docs) {
        if (col === 'orders') {
          const dets = await doc.ref.collection('orderDetails').get();
          for (const d of dets.docs) await d.ref.delete();
        }
        await doc.ref.delete();
      }
    }
  }
  for (const uid of uids) {
    const oldCart = await db.collection('carts').doc(uid).collection('cartDetails').get();
    for (const d of oldCart.docs) await d.ref.delete();
  }
  console.log('Da don data test cu.');

  // ── USER 1: day du nhat — 3 don hang, gio hang, favorites, notifications ──
  let uid = uids[0], u = USERS[0];
  await seedCustomer(uid, u, 30);
  await seedCart(uid, [ { ...P(0), quantity: 1 }, { ...P(5), quantity: 2 } ]);
  await seedOrder(uid, { items: [{ ...P(1), quantity: 1 }, { ...P(2), quantity: 1 }], paymentMethod: 'BANK_TRANSFER', paymentStatus: 'PAID', orderStatus: 'DELIVERED', address: u.address, createdDaysAgo: 20 });
  await seedOrder(uid, { items: [{ ...P(16), quantity: 1 }], paymentMethod: 'BANK_TRANSFER', paymentStatus: 'PAID', orderStatus: 'SHIPPED', address: u.address, createdDaysAgo: 5 });
  await seedOrder(uid, { items: [{ ...P(3), quantity: 2 }], paymentMethod: 'COD', paymentStatus: 'PENDING', orderStatus: 'PENDING', address: u.address, createdDaysAgo: 1 });
  await seedFavorites(uid, [products[1].id, products[16].id, products[10].id]);
  await seedNotifications(uid, [
    { message: 'Đơn hàng GLS-2026-0001 đã giao thành công!', status: 'READ', type: 'ORDER', d: 18 },
    { message: 'Đơn hàng GLS-2026-0002 đang được vận chuyển.', status: 'UNREAD', type: 'ORDER', d: 4 },
    { message: 'Giảm 20% bộ sưu tập Luxury tuần này!', status: 'UNREAD', type: 'PROMOTION', d: 2 },
  ]);
  console.log('User 1 OK:', u.email);

  // ── USER 2: 1 don da giao + gio hang 3 items ──
  uid = uids[1]; u = USERS[1];
  await seedCustomer(uid, u, 25);
  await seedCart(uid, [ { ...P(7), quantity: 1 }, { ...P(9), quantity: 1 }, { ...P(12), quantity: 1 } ]);
  await seedOrder(uid, { items: [{ ...P(17), quantity: 1 }], paymentMethod: 'BANK_TRANSFER', paymentStatus: 'PAID', orderStatus: 'DELIVERED', address: u.address, createdDaysAgo: 10 });
  await seedFavorites(uid, [products[17].id, products[7].id]);
  await seedNotifications(uid, [ { message: 'Đơn hàng của bạn đã giao thành công!', status: 'READ', type: 'ORDER', d: 8 } ]);
  console.log('User 2 OK:', u.email);

  // ── USER 3: 2 don (1 huy, 1 dang xu ly COD), khong gio hang ──
  uid = uids[2]; u = USERS[2];
  await seedCustomer(uid, u, 15);
  await seedOrder(uid, { items: [{ ...P(11), quantity: 1 }], paymentMethod: 'BANK_TRANSFER', paymentStatus: 'PENDING', orderStatus: 'CANCELLED', address: u.address, createdDaysAgo: 7 });
  await seedOrder(uid, { items: [{ ...P(4), quantity: 1 }, { ...P(8), quantity: 1 }], paymentMethod: 'COD', paymentStatus: 'PENDING', orderStatus: 'PROCESSING', address: u.address, createdDaysAgo: 2 });
  await seedFavorites(uid, [products[4].id]);
  await seedNotifications(uid, [ { message: 'Đơn hàng của bạn đã bị hủy.', status: 'READ', type: 'ORDER', d: 7 } ]);
  console.log('User 3 OK:', u.email);

  // ── USER 4: chi co gio hang + nhieu favorites, chua mua gi ──
  uid = uids[3]; u = USERS[3];
  await seedCustomer(uid, u, 5);
  await seedCart(uid, [ { ...P(18), quantity: 1 } ]);
  await seedFavorites(uid, [products[18].id, products[19].id, products[0].id, products[6].id]);
  await seedNotifications(uid, [ { message: 'Chào mừng bạn đến với Glassity!', status: 'UNREAD', type: 'SYSTEM', d: 5 } ]);
  console.log('User 4 OK:', u.email);

  // ── USER 5: account moi tinh — de test empty states ──
  uid = uids[4]; u = USERS[4];
  await seedCustomer(uid, u, 0);
  console.log('User 5 OK:', u.email, '(trong — test empty state)');

  console.log('\n=== XONG! Dang nhap voi password: ' + PASSWORD + ' ===');
}

main().then(() => process.exit(0)).catch(e => { console.error('LOI:', e); process.exit(1); });
