/**
 * read_products.js — In toàn bộ products trong Firestore ra console
 * Chạy: node read_products.js
 */
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const sa = require('./service-account.json');
initializeApp({ credential: cert(sa) });
const db = getFirestore();

async function run() {
  const snap = await db.collection('products').get();
  console.log(`Tổng: ${snap.size} products\n`);
  snap.forEach(doc => {
    const d = doc.data();
    console.log(`ID: ${doc.id}`);
    console.log(`  name       : ${d.name}`);
    console.log(`  price      : ${d.price}`);
    console.log(`  categoryId : ${d.categoryId}`);
    console.log(`  faceShapes : ${JSON.stringify(d.faceShapes)}`);
    console.log(`  gender     : ${d.gender}`);
    console.log(`  images     : ${JSON.stringify(d.images)}`);
    console.log('');
  });
  process.exit(0);
}
run().catch(e => { console.error(e.message); process.exit(1); });
