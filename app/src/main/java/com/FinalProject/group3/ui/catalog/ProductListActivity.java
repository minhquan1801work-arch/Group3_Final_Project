package com.FinalProject.group3.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.R;
import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.databinding.ActivityProductListBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.ProductRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * DL.Product — danh sách sản phẩm. [B3 rework v2 — 09/07/2026]
 *
 * 5 chip filter khớp cấu trúc hamburger, CHỌN RIÊNG từng nhóm (loại trừ nhau),
 * duy nhất Gọng + Shape combine được với nhau (cả 2 đều thuộc Kính Cận):
 *   1. Kính Mát            — toggle
 *   2. Gọng Kính ⌄         — sheet: Nhựa / Kim Loại       → ép Kính Cận
 *   3. Shape Kính ⌄        — sheet: Tròn/Oval/Mắt Mèo/Vuông (HÌNH DÁNG GỌNG) → ép Kính Cận
 *   4. Phụ Kiện ⌄          — sheet: Tất cả / Hộp / Khăn / Nước
 *   5. Bộ Sưu Tập ⌄        — sheet: Tất cả / 3 BST
 *
 * PHÂN BIỆT 2 KHÁI NIỆM SHAPE:
 *   - faceShapes  = DÁNG KHUÔN MẶT phù hợp (từ trang chủ "Chọn kính theo dáng mặt")
 *   - frameShape  = HÌNH DÁNG GỌNG (chip Shape Kính ở đây)
 * Vào từ trang chủ theo dáng mặt → title "Kính hợp mặt X" GIỮ NGUYÊN suốt phiên,
 * các chip lọc thêm bên trong tập đó.
 */
public class ProductListActivity extends AppCompatActivity {

    // ── Category IDs ──
    public static final String CAT_KINH_MAT = "kinh_mat";
    public static final String CAT_KINH_CAN = "kinh_can";
    public static final String CAT_PHU_KIEN = "phu_kien";
    public static final String CAT_BST      = "bst"; // legacy

    // ── Shape values (dùng chung cho faceShapes lẫn frameShape) ──
    public static final String SHAPE_TRON       = "tron";
    public static final String SHAPE_TRAI_XOAN  = "trai_xoan";
    public static final String SHAPE_TRAI_TIM   = "trai_tim";
    public static final String SHAPE_KIM_CUONG  = "kim_cuong";
    public static final String SHAPE_VUONG      = "vuong";
    public static final String SHAPE_OVAL       = "oval";     // frameShape
    public static final String SHAPE_MAT_MEO    = "mat_meo";  // frameShape

    // ── Material / accessory values ──
    public static final String MAT_NHUA     = "nhua";
    public static final String MAT_KIM_LOAI = "kim_loai";
    public static final String ACC_HOP_DUNG = "hop_dung";
    public static final String ACC_KHAN_LAU = "khan_lau";
    public static final String ACC_NUOC_LAU = "nuoc_lau";

    private static final int SORT_DEFAULT = 0;
    private static final int SORT_ASC     = 1;
    private static final int SORT_DESC    = 2;

    private static final String EXTRA_CATEGORY_ID   = "extra_category_id";
    private static final String EXTRA_FACE_SHAPE    = "extra_face_shape";   // dáng mặt (Home)
    private static final String EXTRA_FRAME_SHAPE   = "extra_frame_shape";  // hình dáng gọng (drawer)
    private static final String EXTRA_ALL_SHAPES    = "extra_all_shapes";
    private static final String EXTRA_CATEGORY_NAME = "extra_category_name"; // legacy, bỏ qua
    private static final String EXTRA_COLLECTION    = "extra_collection";
    private static final String EXTRA_MATERIAL      = "extra_material";
    private static final String EXTRA_ACCESSORY     = "extra_accessory";

    // ── Static launchers ──────────────────────────────────────────────────────
    public static void start(Context context, String categoryId, String faceShape, String title) {
        Intent intent = new Intent(context, ProductListActivity.class);
        if (categoryId != null) intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        if (faceShape  != null) intent.putExtra(EXTRA_FACE_SHAPE, faceShape);
        intent.putExtra(EXTRA_CATEGORY_NAME, title);
        context.startActivity(intent);
    }

    public static void startAll(Context context) {
        context.startActivity(new Intent(context, ProductListActivity.class));
    }

    public static void startAllShapes(Context context) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_ALL_SHAPES, true);
        context.startActivity(intent);
    }

    public static void startCollection(Context context, String collection) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_COLLECTION, collection);
        context.startActivity(intent);
    }

    public static void startMaterial(Context context, String material) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_MATERIAL, material);
        context.startActivity(intent);
    }

    /** Lọc theo HÌNH DÁNG GỌNG (drawer "Shape Kính") — khác dáng mặt */
    public static void startFrameShape(Context context, String frameShape) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_FRAME_SHAPE, frameShape);
        context.startActivity(intent);
    }

    /** Lọc phụ kiện: type null = tất cả phụ kiện */
    public static void startAccessory(Context context, String accessoryType) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_CATEGORY_ID, CAT_PHU_KIEN);
        if (accessoryType != null) intent.putExtra(EXTRA_ACCESSORY, accessoryType);
        context.startActivity(intent);
    }

    // ── Filter state ──────────────────────────────────────────────────────────
    /** 4 nhóm loại trừ nhau (chỉ 1 active tại 1 thời điểm) */
    private enum Mode { NONE, KINH_MAT, KINH_CAN, PHU_KIEN, BST }

    private Mode   mode = Mode.NONE;
    private String selMaterial   = null; // sub của KINH_CAN
    private String selFrameShape = null; // sub của KINH_CAN (combine được với material)
    private String selAccessory  = null; // sub của PHU_KIEN; null = tất cả
    private String selCollection = null; // sub của BST; "" = tất cả

    /** Ngữ cảnh cố định từ trang chủ — GIỮ suốt phiên, title luôn hiện */
    private String  fixedFaceShape = null;
    private boolean fixedAllShapes = false;

    private int currentSort = SORT_DEFAULT;

    private ActivityProductListBinding binding;
    private ProductAdapter productAdapter;
    private final List<Product> allProducts = new ArrayList<>();
    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        readExtras();

        binding.btnBack.setOnClickListener(v -> finish());
        productAdapter = new ProductAdapter(product ->
                ProductDetailActivity.start(this, product.getProductId()));
        com.FinalProject.group3.utils.CartQuickActions.wire(productAdapter, this);
        binding.rvProducts.setAdapter(productAdapter);

        setupChips();
        binding.btnFilter.setOnClickListener(v -> showSortSheet());

        // Ngữ cảnh dáng mặt = đang chọn KÍNH → ẩn chip Phụ Kiện
        // (phụ kiện không có faceShapes, giao 2 tập luôn rỗng — phi logic)
        if (fixedFaceShape != null || fixedAllShapes) {
            binding.chipPhuKien.setVisibility(View.GONE);
        }

        loadAllProducts();
    }

    private void readExtras() {
        fixedFaceShape = getIntent().getStringExtra(EXTRA_FACE_SHAPE);
        fixedAllShapes = getIntent().getBooleanExtra(EXTRA_ALL_SHAPES, false);

        String cat = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        String material   = getIntent().getStringExtra(EXTRA_MATERIAL);
        String frameShape = getIntent().getStringExtra(EXTRA_FRAME_SHAPE);
        String accessory  = getIntent().getStringExtra(EXTRA_ACCESSORY);
        String collection = getIntent().getStringExtra(EXTRA_COLLECTION);

        if (collection != null) {
            mode = Mode.BST; selCollection = collection;
        } else if (material != null || frameShape != null) {
            mode = Mode.KINH_CAN; selMaterial = material; selFrameShape = frameShape;
        } else if (CAT_PHU_KIEN.equals(cat)) {
            mode = Mode.PHU_KIEN; selAccessory = accessory;
        } else if (CAT_KINH_MAT.equals(cat)) {
            mode = Mode.KINH_MAT;
        } else if (CAT_KINH_CAN.equals(cat)) {
            mode = Mode.KINH_CAN;
        } else if (CAT_BST.equals(cat)) {
            mode = Mode.BST; selCollection = "";
        }
    }

    // ── Load 1 lần → filter RAM ───────────────────────────────────────────────
    private void loadAllProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);
        new ProductRepository().getAllProducts(new ProductRepository.ProductListCallback() {
            @Override public void onSuccess(List<Product> products) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                allProducts.clear();
                allProducts.addAll(products);
                loaded = true;
                applyFilters();
            }
            @Override public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(ProductListActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        if (!loaded) return;

        List<Product> result = new ArrayList<>();
        for (Product p : allProducts) {
            // Ngữ cảnh dáng mặt cố định
            if (fixedFaceShape != null
                    && (p.getFaceShapes() == null || !p.getFaceShapes().contains(fixedFaceShape))) continue;
            if (fixedAllShapes
                    && (p.getFaceShapes() == null || p.getFaceShapes().isEmpty())) continue;

            switch (mode) {
                case KINH_MAT:
                    if (!CAT_KINH_MAT.equals(p.getCategoryId())) continue;
                    break;
                case KINH_CAN:
                    if (!CAT_KINH_CAN.equals(p.getCategoryId())) continue;
                    if (selMaterial != null && !selMaterial.equals(materialOf(p))) continue;
                    if (selFrameShape != null && !selFrameShape.equals(frameShapeOf(p))) continue;
                    break;
                case PHU_KIEN:
                    if (!CAT_PHU_KIEN.equals(p.getCategoryId())) continue;
                    if (selAccessory != null && !selAccessory.equals(p.getAccessoryType())) continue;
                    break;
                case BST:
                    if (p.getCollection() == null || p.getCollection().isEmpty()) continue;
                    if (selCollection != null && !selCollection.isEmpty()
                            && !selCollection.equals(p.getCollection())) continue;
                    break;
                case NONE:
                default:
                    break;
            }
            result.add(p);
        }

        if (currentSort == SORT_ASC)
            Collections.sort(result, (a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        else if (currentSort == SORT_DESC)
            Collections.sort(result, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));

        productAdapter.submitList(result);
        binding.tvEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
        updateTitle();
        refreshChipStates();
    }

    // ── Field mới, fallback heuristic cho doc cũ chưa có ─────────────────────
    private String materialOf(Product p) {
        if (p.getMaterial() != null && !p.getMaterial().isEmpty()) return p.getMaterial();
        String text = normalize(p.getName() + " " + p.getDescription());
        return (text.contains("metal") || text.contains("kim loai") || text.contains("titan"))
                ? MAT_KIM_LOAI : MAT_NHUA;
    }

    private String frameShapeOf(Product p) {
        if (p.getFrameShape() != null && !p.getFrameShape().isEmpty()) return p.getFrameShape();
        String text = normalize(p.getName() + " " + p.getDescription());
        if (text.contains("cat eye") || text.contains("mat meo")) return SHAPE_MAT_MEO;
        if (text.contains("square") || text.contains("vuong") || text.contains("chu nhat")) return SHAPE_VUONG;
        if (text.contains("round") || text.contains("tron")) return SHAPE_TRON;
        if (text.contains("oval")) return SHAPE_OVAL;
        return "";
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D');
        return n.toLowerCase(Locale.US);
    }

    // ── Title ─────────────────────────────────────────────────────────────────
    private void updateTitle() {
        List<String> parts = new ArrayList<>();
        if (fixedFaceShape != null) parts.add("Kính hợp mặt " + faceLabel(fixedFaceShape));
        if (fixedAllShapes)         parts.add("Kính theo dáng mặt");

        switch (mode) {
            case KINH_MAT: parts.add("Kính Mát"); break;
            case KINH_CAN:
                parts.add("Kính Cận");
                if (MAT_NHUA.equals(selMaterial))     parts.add("Gọng Nhựa");
                if (MAT_KIM_LOAI.equals(selMaterial)) parts.add("Gọng Kim Loại");
                if (selFrameShape != null)            parts.add("Shape " + frameLabel(selFrameShape));
                break;
            case PHU_KIEN:
                parts.add("Phụ Kiện");
                if (selAccessory != null) parts.add(accLabel(selAccessory));
                break;
            case BST:
                parts.add("Bộ Sưu Tập");
                if (selCollection != null && !selCollection.isEmpty()) {
                    parts.remove(parts.size() - 1); // tên BST cụ thể thay chữ chung
                    parts.add(selCollection);
                }
                break;
            default: break;
        }

        binding.tvTitle.setText(parts.isEmpty() ? "Tất cả sản phẩm" : String.join(" · ", parts));
    }

    private String faceLabel(String shape) {
        switch (shape) {
            case SHAPE_TRON:      return "Tròn";
            case SHAPE_TRAI_XOAN: return "Trái Xoan";
            case SHAPE_TRAI_TIM:  return "Trái Tim";
            case SHAPE_KIM_CUONG: return "Kim Cương";
            case SHAPE_VUONG:     return "Vuông";
            default:              return shape;
        }
    }

    private String frameLabel(String shape) {
        switch (shape) {
            case SHAPE_TRON:    return "Tròn";
            case SHAPE_OVAL:    return "Oval";
            case SHAPE_MAT_MEO: return "Mắt Mèo";
            case SHAPE_VUONG:   return "Vuông";
            default:            return shape;
        }
    }

    private String accLabel(String type) {
        switch (type) {
            case ACC_HOP_DUNG: return "Hộp Đựng Kính";
            case ACC_KHAN_LAU: return "Khăn Lau Kính";
            case ACC_NUOC_LAU: return "Nước Lau Kính";
            default:           return type;
        }
    }

    // ── Chips ─────────────────────────────────────────────────────────────────
    private void setupChips() {
        binding.chipKinhMat.setOnClickListener(v -> {
            if (mode == Mode.KINH_MAT) { clearMode(); }
            else { clearMode(); mode = Mode.KINH_MAT; }
            applyFilters();
        });
        binding.chipGong.setOnClickListener(v -> showMaterialSheet());
        binding.chipShape.setOnClickListener(v -> showFrameShapeSheet());
        binding.chipPhuKien.setOnClickListener(v -> showAccessorySheet());
        binding.chipLuxury.setOnClickListener(v -> showCollectionSheet());
    }

    private void clearMode() {
        mode = Mode.NONE;
        selMaterial = null;
        selFrameShape = null;
        selAccessory = null;
        selCollection = null;
    }

    private void refreshChipStates() {
        setChip(binding.chipKinhMat, mode == Mode.KINH_MAT);
        setChip(binding.chipGong,    mode == Mode.KINH_CAN && selMaterial != null);
        setChip(binding.chipShape,   mode == Mode.KINH_CAN && selFrameShape != null);
        setChip(binding.chipPhuKien, mode == Mode.PHU_KIEN);
        setChip(binding.chipLuxury,  mode == Mode.BST);

        binding.chipGong.setText(selMaterial == null ? "Gọng Kính ⌄"
                : "Gọng: " + (MAT_NHUA.equals(selMaterial) ? "Nhựa" : "Kim Loại") + " ⌄");
        binding.chipShape.setText(selFrameShape == null ? "Shape Kính ⌄"
                : "Shape: " + frameLabel(selFrameShape) + " ⌄");
        binding.chipPhuKien.setText(mode != Mode.PHU_KIEN ? "Phụ Kiện ⌄"
                : (selAccessory == null ? "Phụ Kiện: Tất cả ⌄" : "PK: " + accLabel(selAccessory) + " ⌄"));
        binding.chipLuxury.setText(mode != Mode.BST ? "Bộ Sưu Tập ⌄"
                : (selCollection == null || selCollection.isEmpty()
                        ? "BST: Tất cả ⌄" : "BST: " + shortCollection(selCollection) + " ⌄"));
    }

    private String shortCollection(String c) {
        if (c == null) return "";
        return c.replace(" Collection", "").replace(" Acetate", "").replace(" Studio", "");
    }

    private void setChip(TextView chip, boolean active) {
        chip.setBackgroundResource(active
                ? R.drawable.bg_btn_wine_filled : R.drawable.bg_btn_wine_outline);
        chip.setTextColor(active ? 0xFFFFFFFF : getColor(R.color.brand_wine));
    }

    // ── Sheets ────────────────────────────────────────────────────────────────

    private void showMaterialSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottom_sheet_material, null);
        sheet.setContentView(v);
        v.findViewById(R.id.itemMatNhua).setOnClickListener(x -> {
            sheet.dismiss(); enterKinhCan(MAT_NHUA, selFrameShape);
        });
        v.findViewById(R.id.itemMatKimLoai).setOnClickListener(x -> {
            sheet.dismiss(); enterKinhCan(MAT_KIM_LOAI, selFrameShape);
        });
        v.findViewById(R.id.itemMatClear).setOnClickListener(x -> {
            sheet.dismiss();
            selMaterial = null;
            if (selFrameShape == null) mode = Mode.NONE;
            applyFilters();
        });
        sheet.show();
    }

    private void showFrameShapeSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottom_sheet_shape, null);
        sheet.setContentView(v);

        int[] ids = { R.id.itemShapeTron, R.id.itemShapeOval, R.id.itemShapeMatMeo, R.id.itemShapeVuong };
        String[] vals = { SHAPE_TRON, SHAPE_OVAL, SHAPE_MAT_MEO, SHAPE_VUONG };
        for (int i = 0; i < ids.length; i++) {
            final String val = vals[i];
            View item = v.findViewById(ids[i]);
            if (item != null) item.setOnClickListener(x -> {
                sheet.dismiss(); enterKinhCan(selMaterial, val);
            });
        }
        View clear = v.findViewById(R.id.itemShapeAll);
        if (clear != null) clear.setOnClickListener(x -> {
            sheet.dismiss();
            selFrameShape = null;
            if (selMaterial == null) mode = Mode.NONE;
            applyFilters();
        });
        sheet.show();
    }

    /** Vào chế độ Kính Cận: giữ được gọng + shape cùng lúc, clear các nhóm khác. */
    private void enterKinhCan(String material, String frameShape) {
        boolean wasKinhCan = (mode == Mode.KINH_CAN);
        clearMode();
        mode = Mode.KINH_CAN;
        selMaterial   = material;
        selFrameShape = frameShape;
        if (!wasKinhCan) {
            // đổi từ nhóm khác sang: chỉ giữ giá trị vừa chọn (tham số đã phản ánh)
        }
        applyFilters();
    }

    private void showAccessorySheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottom_sheet_accessory, null);
        sheet.setContentView(v);
        v.findViewById(R.id.itemAccAll).setOnClickListener(x -> {
            sheet.dismiss(); clearMode(); mode = Mode.PHU_KIEN; selAccessory = null; applyFilters();
        });
        v.findViewById(R.id.itemAccHopDung).setOnClickListener(x -> {
            sheet.dismiss(); clearMode(); mode = Mode.PHU_KIEN; selAccessory = ACC_HOP_DUNG; applyFilters();
        });
        v.findViewById(R.id.itemAccKhanLau).setOnClickListener(x -> {
            sheet.dismiss(); clearMode(); mode = Mode.PHU_KIEN; selAccessory = ACC_KHAN_LAU; applyFilters();
        });
        v.findViewById(R.id.itemAccNuocLau).setOnClickListener(x -> {
            sheet.dismiss(); clearMode(); mode = Mode.PHU_KIEN; selAccessory = ACC_NUOC_LAU; applyFilters();
        });
        v.findViewById(R.id.itemAccClear).setOnClickListener(x -> {
            sheet.dismiss();
            if (mode == Mode.PHU_KIEN) clearMode();
            applyFilters();
        });
        sheet.show();
    }

    private void showCollectionSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottom_sheet_collection, null);
        sheet.setContentView(v);
        v.findViewById(R.id.itemColAll).setOnClickListener(x -> {
            sheet.dismiss(); clearMode(); mode = Mode.BST; selCollection = ""; applyFilters();
        });
        v.findViewById(R.id.itemColMonochrome).setOnClickListener(x -> {
            sheet.dismiss(); clearMode(); mode = Mode.BST; selCollection = "Monochrome Collection"; applyFilters();
        });
        v.findViewById(R.id.itemColEssential).setOnClickListener(x -> {
            sheet.dismiss(); clearMode(); mode = Mode.BST; selCollection = "Essential Acetate"; applyFilters();
        });
        v.findViewById(R.id.itemColSunlight).setOnClickListener(x -> {
            sheet.dismiss(); clearMode(); mode = Mode.BST; selCollection = "Sunlight Studio"; applyFilters();
        });
        v.findViewById(R.id.itemColClear).setOnClickListener(x -> {
            sheet.dismiss();
            if (mode == Mode.BST) clearMode();
            applyFilters();
        });
        sheet.show();
    }

    // ── Sort ──────────────────────────────────────────────────────────────────
    private void showSortSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
        sheet.setContentView(sheetView);

        RadioGroup rg   = sheetView.findViewById(R.id.rgSort);
        RadioButton asc = sheetView.findViewById(R.id.rbSortAsc);
        RadioButton dsc = sheetView.findViewById(R.id.rbSortDesc);
        if (currentSort == SORT_ASC)  asc.setChecked(true);
        if (currentSort == SORT_DESC) dsc.setChecked(true);

        rg.setOnCheckedChangeListener((group, id) -> {
            currentSort = (id == R.id.rbSortAsc) ? SORT_ASC : SORT_DESC;
            applyFilters();
            sheet.dismiss();
        });
        sheet.show();
    }
}
