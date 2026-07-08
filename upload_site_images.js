const { v2: cloudinary } = require('cloudinary');
cloudinary.config(require('./cloudinary-config.json'));

const path = require('path');
const ROOT = 'D:/Glassity/photo-20260706T105430Z-3-001/photo';

const FILES = [
  ['bg1.png', 'hero_bg1'],
  ['bg2.png', 'hero_bg2'],
  ['bg3.png', 'hero_bg3'],
  ['background.png', 'flatlay_background'],
  ['guide.png', 'guide_diagram'],
  ['glass1.png', 'glass1'],
  ['shape1.png', 'shape_tron'],
  ['Shape2.png', 'shape_trai_xoan'],
  ['shape3.png', 'shape_trai_tim'],
  ['shape4.png', 'shape_kim_cuong'],
  ['shpe5.png', 'shape_vuong'],
];

async function main() {
  for (const [file, publicName] of FILES) {
    const filePath = path.join(ROOT, file);
    process.stdout.write(`Uploading ${file}... `);
    const result = await cloudinary.uploader.upload(filePath, {
      public_id: `glassity/site/${publicName}`,
      overwrite: true,
      resource_type: 'image',
    });
    console.log('OK -> ' + result.secure_url);
  }
}

main().catch(e => { console.error(e); process.exit(1); });