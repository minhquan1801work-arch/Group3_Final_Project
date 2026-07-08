/**
 * upload_products.js
 *
 * 1. Upload tất cả ảnh từ D:\Glassity\...\kính\ lên Cloudinary (folder glassity/products/<tên sp>)
 * 2. Tạo/update document Firestore trong collection "products" với:
 *    - name: tên folder (title case)
 *    - categoryId: "kinh_mat"
 *    - variants: [{ color: "", stock: 0, images: [url] }] — mỗi ảnh = 1 variant
 *    - faceShapes: []  (điền sau)
 *    - gender: "unisex"
 *    - price: 0        (điền sau)
 *    - description: ""
 *
 * Chạy: node upload_products.js
 */

const { v2: cloudinary } = require('cloudinary');
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const fs   = require('fs');
const path = require('path');

// ── Cấu hình ─────────────────────────────────────────────────────────────────
cloudinary.config(require('./cloudinary-config.json'));

const sa = require('./service-account.json');
initializeApp({ credential: cert(sa) });
const db = getFirestore();

const IMAGES_ROOT = 'D:/Glassity/kính-20260706T105438Z-3-001/kính';

// Chuyển "BOLD SQUARE FRAME SUNGLASSES" → "Bold Square Frame Sunglasses"
function toTitleCase(str) {
    return str.toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
}

// Upload 1 ảnh lên Cloudinary, trả về secure_url
async function uploadImage(filePath, publicId) {
    const result = await cloudinary.uploader.upload(filePath, {
        public_id:    publicId,
        folder:       '', // publicId đã chứa folder
        overwrite:    true,
        resource_type: 'image',
    });
    return result.secure_url;
}

async function main() {
    const productFolders = fs.readdirSync(IMAGES_ROOT).filter(f => {
        return fs.statSync(path.join(IMAGES_ROOT, f)).isDirectory();
    });

    console.log(`Found ${productFolders.length} products.\n`);

    for (const folderName of productFolders) {
        const folderPath = path.join(IMAGES_ROOT, folderName);
        const imageFiles = fs.readdirSync(folderPath)
            .filter(f => /\.(png|jpg|jpeg|webp)$/i.test(f))
            .sort((a, b) => {
                // sort theo số trong tên file (1.png < 2.png < 10.png)
                const numA = parseInt(a);
                const numB = parseInt(b);
                return numA - numB;
            });

        if (imageFiles.length === 0) {
            console.log(`SKIP (no images): ${folderName}`);
            continue;
        }

        console.log(`\n[${folderName}] — ${imageFiles.length} image(s)`);

        // Upload từng ảnh
        const variants = [];
        for (const imgFile of imageFiles) {
            const imgPath  = path.join(folderPath, imgFile);
            const baseName = path.basename(imgFile, path.extname(imgFile));
            const publicId = `glassity/products/${folderName}/${baseName}`;

            try {
                process.stdout.write(`  Uploading ${imgFile}... `);
                const url = await uploadImage(imgPath, publicId);
                console.log(`OK → ${url}`);
                variants.push({
                    color:  '',    // bạn điền hex color sau trên Firebase
                    stock:  0,     // bạn điền stock sau
                    images: [url],
                });
            } catch (err) {
                console.log(`ERROR: ${err.message}`);
            }
        }

        if (variants.length === 0) continue;

        // Tìm doc hiện tại theo name (để tránh tạo trùng)
        const productName = toTitleCase(folderName);
        const existing = await db.collection('products')
            .where('name', '==', productName)
            .limit(1)
            .get();

        const data = {
            name:        productName,
            categoryId:  'kinh_mat',
            variants,
            faceShapes:  [],
            gender:      'unisex',
            price:       0,
            description: '',
            collection:  '',
        };

        if (!existing.empty) {
            await existing.docs[0].ref.update(data);
            console.log(`  → Firestore UPDATED: ${productName}`);
        } else {
            await db.collection('products').add(data);
            console.log(`  → Firestore CREATED: ${productName}`);
        }
    }

    console.log('\n✓ All done!');
}

main().catch(err => {
    console.error('Fatal:', err);
    process.exit(1);
});
