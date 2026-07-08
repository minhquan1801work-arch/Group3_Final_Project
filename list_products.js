const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const sa = require('./service-account.json');
initializeApp({ credential: cert(sa) });
const db = getFirestore();

(async () => {
  const snap = await db.collection('products').get();
  console.log('Total docs:', snap.size);
  for (const doc of snap.docs) {
    const d = doc.data();
    const nVar = d.variants ? d.variants.length : 0;
    const img = d.variants && d.variants[0] && d.variants[0].images ? d.variants[0].images[0] : (d.images ? d.images[0] : '');
    console.log(`${doc.id} | ${d.name} | cat=${d.categoryId} | ${nVar} variants | ${(img||'').substring(0,60)}`);
  }
})();