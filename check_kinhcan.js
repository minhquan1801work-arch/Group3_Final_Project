const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
initializeApp({ credential: cert(require('./service-account.json')) });
const db = getFirestore();
(async () => {
  const snap = await db.collection('products').where('categoryId', '==', 'kinh_can').get();
  console.log('kinh_can docs:', snap.size);
  for (const d of snap.docs) {
    const x = d.data();
    console.log(`- ${x.name} | faceShapes=${JSON.stringify(x.faceShapes)} | desc="${(x.description||'').substring(0,50)}"`);
  }
})();