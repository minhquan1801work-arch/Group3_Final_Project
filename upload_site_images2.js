const { v2: cloudinary } = require('cloudinary');
cloudinary.config(require('./cloudinary-config.json'));
const path = require('path');
const ROOT = 'D:/Glassity/photo-20260706T105430Z-3-001/photo';

const FILES = [
  ['sasalele.png', 'promo_sasalele'],
  ['1.jpg', 'kham_pha_flowers'],
  ['james.heic', 'hero_james'],
  ['juhoon.jpg', 'hero_juhoon'],
  ['karina.jpg', 'karina'],
  ['login_signup.jpg', 'blog_login_signup'],
];

async function main() {
  for (const [file, publicName] of FILES) {
    const filePath = path.join(ROOT, file);
    process.stdout.write(`Uploading ${file}... `);
    const result = await cloudinary.uploader.upload(filePath, {
      public_id: `glassity/site/${publicName}`,
      overwrite: true,
      resource_type: 'image',
      format: 'jpg', // heic -> jpg de Android load duoc
    });
    console.log('OK -> ' + result.secure_url);
  }
}
main().catch(e => { console.error(e); process.exit(1); });