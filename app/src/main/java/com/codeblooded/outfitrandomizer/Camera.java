package com.codeblooded.outfitrandomizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.yalantis.ucrop.UCrop;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class Camera extends AppCompatActivity {

    // Static block must be at class level, not inside onCreate
    static {
        System.loadLibrary("opencv_java4");
    }

    private static final int REQUEST_CODE_CAMERA = 1001;

    private PreviewView previewView;
    private FrameLayout loadingOverlay;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.preview_view_camera);
        Button captureBtn = findViewById(R.id.capture_button_camera);
        loadingOverlay = findViewById(R.id.loading_overlay_camera);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_CAMERA
            );
        }

        captureBtn.setOnClickListener(v -> capturePhoto());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to start camera.", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);

                        Bitmap bitmap = imageProxyToBitmap(image);
                        image.close();

                        runOnUiThread(() -> {
                            showLoading(false);
                            launchCrop(bitmap);
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                        exception.printStackTrace();
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(
                                    Camera.this,
                                    "Failed to capture photo.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                }
        );
    }

    private void launchCrop(Bitmap bitmap) {
        try {
            File sourceFile = new File(getFilesDir(), "capture_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(sourceFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            String auth = getPackageName() + ".provider";
            Uri sourceUri = FileProvider.getUriForFile(this, auth, sourceFile);

            File destinationFile = new File(getFilesDir(), "cropped_" + System.currentTimeMillis() + ".jpg");
            Uri destinationUri = Uri.fromFile(destinationFile);

            UCrop.of(sourceUri, destinationUri).withMaxResultSize(1080, 1080).start(this);
        } catch (IOException except) {
            except.printStackTrace();
            Toast.makeText(this, "Failed to start crop.", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        // ImageCapture output is JPEG → single plane (index 0) with full JPEG bytes
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // Handle rotation from the camera sensor so your Bitmap is upright
        int rotationDegrees = image.getImageInfo().getRotationDegrees();
        if (rotationDegrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            bitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
            );
        }

        return bitmap;
    }

    private Bitmap removeBackground(Bitmap src) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(src, rgba);
        // convert to bgr for GrabCut
        Mat bgr = new Mat();
        Imgproc.cvtColor(rgba, bgr, Imgproc.COLOR_RGBA2BGR);

        int width = bgr.cols();
        int height = bgr.rows();
        // create mask
        Mat mask = new Mat(bgr.size(), CvType.CV_8UC1, new Scalar(Imgproc.GC_PR_BGD));

        int rectX = (int) (width * 0.1);
        int rectY = (int) (height * 0.1);
        int rectW = (int) (width * 0.8);
        int rectH = (int) (height * 0.8);
        org.opencv.core.Rect rect = new org.opencv.core.Rect(rectX, rectY, rectW, rectH);
        // mask foreground
        mask.submat(rect).setTo(new Scalar(Imgproc.GC_PR_FGD));
        // run GrabCut
        Mat bgdModel = new Mat();
        Mat fgdModel = new Mat();
        Imgproc.grabCut(bgr, mask, rect, bgdModel, fgdModel, 3, Imgproc.GC_INIT_WITH_RECT);
        //binary foreground mask
        Mat maskFG = new Mat(mask.size(), CvType.CV_8UC1);
        Mat tmp = new Mat(mask.size(), CvType.CV_8UC1);

        Core.compare(mask, new Scalar(Imgproc.GC_FGD), maskFG, Core.CMP_EQ);
        Core.compare(mask, new Scalar(Imgproc.GC_PR_FGD), tmp, Core.CMP_EQ);
        Core.bitwise_or(maskFG, tmp, maskFG);
        // smooth edges
        Imgproc.medianBlur(maskFG, maskFG, 5);
        // create rgba result, connect mask to alpha
        Mat result = new Mat();
        Imgproc.cvtColor(bgr, result, Imgproc.COLOR_BGR2RGBA);

        java.util.List<Mat> channels = new java.util.ArrayList<>(4);
        Core.split(result, channels);
        //create alpha channel if missing
        if (channels.size() < 4) {
            Mat alpha = new Mat(result.size(), CvType.CV_8UC1, new Scalar(255));
            channels.add(alpha);
        }
        //use mask as alpha (foreground = 255, background = 0)
        channels.set(3, maskFG);
        Core.merge(channels, result);
        // convert to bitmap
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, output);

        // cleanup
        rgba.release();
        bgr.release();
        mask.release();
        bgdModel.release();
        fgdModel.release();
        maskFG.release();
        tmp.release();
        result.release();
        for (Mat c : channels) {
            c.release();
        }

        return output;
    }

    private Uri saveToInternalStorage(Bitmap bitmap) {
        String fileName = "clothing_" + System.currentTimeMillis() + ".png";

        File file = new File(getFilesDir(), fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String authority = getPackageName() + ".provider";
        return FileProvider.getUriForFile(this, authority, file);
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_CAMERA) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(
                        this,
                        "Camera permission is required to capture clothing items.",
                        Toast.LENGTH_LONG
                ).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK && data != null) {
                Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    try {
                        showLoading(true);

                        Bitmap croppedBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                        Bitmap cutoutBitmap = removeBackground(croppedBitmap);
                        Uri imageUri = saveToInternalStorage(cutoutBitmap);

                        showLoading(false);

                        if (imageUri != null) {
                            Intent out = new Intent();
                            out.putExtra("image_uri", imageUri.toString());
                            setResult(RESULT_OK, out);
                            finish();
                        } else {
                            Toast.makeText(this, "Save failed after crop.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception except) {
                        showLoading(false);
                        except.printStackTrace();
                        Toast.makeText(this, "Failed to process cropped image.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
                Throwable error = UCrop.getError(data);
                if (error != null) error.printStackTrace();
                Toast.makeText(this, "Crop canceled or failed.", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) finish();
        }
    }
}
