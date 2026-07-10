const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
initializeApp({ credential: cert(require('./service-account.json')) });
const db = getFirestore();

const NAMES = ['Glassity Urban', 'Glassity Vintage', 'Glassity Wave', 'Glassity Edge', 'Glassity Pilot'];

(async () => {
  const snap = await db.collection('products').where('categoryId', '==', 'kinh_mat').get();
  let deleted = 0;
  for (const doc of snap.docs) {
    if (NAMES.includes(doc.data().name)) {
      await doc.ref.delete();
      console.log('DELETED: ' + doc.data().name + ' (' + doc.id + ')');
      deleted++;
    }
  }
  const after = await db.collection('products').get();
  console.log(`\nDeleted ${deleted}. Total products now: ${after.size}`);
})();