package com.FinalProject.group3.ui.catalog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.media.Image;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.FinalProject.group3.databinding.ActivityTryOnBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thử kính ảo (AR try-on) — CameraX preview camera trước + ML Kit Face Detection
 * lấy landmark 2 mắt/góc nghiêng đầu, vẽ overlay gọng kính demo qua {@link com.FinalProject.group3.utils.FaceOverlayView}.
 * Dùng chung 1 ảnh gọng kính demo cho mọi sản phẩm (chưa khớp màu/dáng từng SP — chỉ chứng minh cơ chế AR).
 */
public class TryOnActivity extends AppCompatActivity {

    public static void start(Context context) {
        context.startActivity(new Intent(context, TryOnActivity.class));
    }

    private ActivityTryOnBinding binding;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private ActivityResultLauncher<String> cameraPermLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTryOnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        cameraExecutor = Executors.newSingleThreadExecutor();
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .build();
        faceDetector = FaceDetection.getClient(options);

        cameraPermLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (Boolean.TRUE.equals(granted)) startCamera();
                    else {
                        Toast.makeText(this, "Cần quyền camera để thử kính ảo", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                bindCameraUseCases(future.get());
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Không mở được camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

        CameraSelector selector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, selector, preview, analysis);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeFrame(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        int rotation = imageProxy.getImageInfo().getRotationDegrees();
        boolean swapDims = rotation == 90 || rotation == 270;
        int srcWidth = swapDims ? mediaImage.getHeight() : mediaImage.getWidth();
        int srcHeight = swapDims ? mediaImage.getWidth() : mediaImage.getHeight();

        InputImage inputImage = InputImage.fromMediaImage(mediaImage, rotation);

        faceDetector.process(inputImage)
                .addOnSuccessListener(faces -> onFacesDetected(faces, srcWidth, srcHeight))
                .addOnFailureListener(e -> { /* bỏ qua frame lỗi, thử tiếp frame sau */ })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void onFacesDetected(List<Face> faces, int srcWidth, int srcHeight) {
        if (binding == null) return;
        binding.overlayView.setImageSourceInfo(srcWidth, srcHeight, true);

        if (faces.isEmpty()) {
            binding.overlayView.clearFace();
            return;
        }

        Face face = faces.get(0);
        FaceLandmark leftEyeLm = face.getLandmark(FaceLandmark.LEFT_EYE);
        FaceLandmark rightEyeLm = face.getLandmark(FaceLandmark.RIGHT_EYE);
        if (leftEyeLm == null || rightEyeLm == null) {
            binding.overlayView.clearFace();
            return;
        }

        PointF leftEye = leftEyeLm.getPosition();
        PointF rightEye = rightEyeLm.getPosition();
        binding.overlayView.updateFace(leftEye, rightEye, face.getHeadEulerAngleZ());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        faceDetector.close();
        binding = null;
    }
}
