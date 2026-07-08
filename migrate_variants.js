/**
 * migrate_variants.js
 *
 * Chuyển cấu trúc product từ:
 *   { colors: ["#hex1","#hex2"], images: ["url1","url2"], stock: 10 }
 * sang:
 *   { variants: [
 *       { color: "#hex1", stock: 5, images: ["url1"] },
 *       { color: "#hex2", stock: 5, images: ["url2"] }
 *   ]}
 *
 * Quy tắc migrate tự động:
 * - Mỗi màu ghép với 1 ảnh theo thứ tự (colors[i] ↔ images[i])
 * - Stock chia đều; số lẻ dồn vào variant đầu tiên
 * - Nếu ảnh nhiều hơn màu, ảnh dư gán vào variant cuối
 *
 * Chạy: node migrate_variants.js
 * Sau khi chạy xong, kiểm tra lại trên Firebase Console trước khi xóa field cũ.
 */

const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const sa = require('./service-account.json');

initializeApp({ credential: cert(sa) });
const db = getFirestore();

async function migrate() {
    const snapshot = await db.collection('products').get();
    const batch = db.batch();
    let count = 0;

    for (const doc of snapshot.docs) {
        const data = doc.data();

        // Bỏ qua nếu đã có variants
        if (data.variants && data.variants.length > 0) {
            console.log(`SKIP (already has variants): ${data.name}`);
            continue;
        }

        const colors = data.colors || [];
        const images = data.images || [];
        const stock  = data.stock  || 0;

        if (colors.length === 0 && images.length === 0) {
            console.log(`SKIP (no colors/images): ${data.name}`);
            continue;
        }

        const variantCount = Math.max(colors.length, 1);
        const baseStock    = Math.floor(stock / variantCount);
        const remainder    = stock % variantCount;

        const variants = [];
        for (let i = 0; i < variantCount; i++) {
            const color     = colors[i] || colors[0] || '#1A1614';
            const varStock  = baseStock + (i === 0 ? remainder : 0);

            // Gán ảnh: mỗi variant lấy images[i], variant cuối lấy hết ảnh thừa
            let varImages = [];
            if (i < variantCount - 1) {
                if (images[i]) varImages = [images[i]];
            } else {
                varImages = images.slice(i);
            }

            variants.push({ color, stock: varStock, images: varImages });
        }

        batch.update(doc.ref, { variants });
        console.log(`MIGRATE: ${data.name} → ${variants.length} variant(s)`);
        count++;
    }

    await batch.commit();
    console.log(`\nDone. ${count} products updated.`);
}

migrate().catch(console.error);
