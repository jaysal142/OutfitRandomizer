package com.codeblooded.outfitrandomizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

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
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.codeblooded.outfitrandomizer.util.BGRemover;
import com.google.common.util.concurrent.ListenableFuture;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class Camera extends AppCompatActivity {

    private static final int REQUEST_CODE_CAMERA = 1001;

    private PreviewView previewView;
    private FrameLayout loadingOverlay;
    private ImageCapture imageCapture;
    private BGRemover bgRemover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);

        bgRemover = new BGRemover(this);
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
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProv = future.get();

                Preview camView = new Preview.Builder().build();
                camView.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProv.unbindAll();
                cameraProv.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        camView,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException except) {
                except.printStackTrace();
                toast("Failed to start camera.");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            toast("Camera not ready yet.");
            return;
        }

        showLoading(true);
        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
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
                            toast("Failed to capture photo.");
                        });
                    }
                }
        );
    }

    private void launchCrop(Bitmap bitmap) {
        try {
            File sourceFile = new File(getFilesDir(), "capture_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream output = new FileOutputStream(sourceFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            }

            String auth = getPackageName() + ".provider";
            Uri sourceUri = FileProvider.getUriForFile(this, auth, sourceFile);

            File destinationFile = new File(getFilesDir(), "cropped_" + System.currentTimeMillis() + ".jpg");
            Uri destinationUri = Uri.fromFile(destinationFile);

            UCrop.of(sourceUri, destinationUri)
                    .withMaxResultSize(1080, 1080)
                    .start(this);
        } catch (IOException except) {
            except.printStackTrace();
            toast("Failed to start crop.");
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        int rotationDegrees = image.getImageInfo().getRotationDegrees();
        if (rotationDegrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            bitmap = Bitmap.createBitmap(
                    bitmap,
                    0, 0,
                    bitmap.getWidth(), bitmap.getHeight(),
                    matrix,
                    true
            );
        }

        return bitmap;
    }

    private Uri saveToInternalStorage(Bitmap bitmap) {
        File file = new File(getFilesDir(), "clothing_" + System.currentTimeMillis() + ".png");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
            toast("Failed to save image.");
            return null;
        }

        String authority = getPackageName() + ".provider";
        return FileProvider.getUriForFile(this, authority, file);
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_CAMERA) return;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != UCrop.REQUEST_CROP) return;

        if (resultCode == RESULT_CANCELED) {
            finish();
            return;
        }

        if (resultCode == UCrop.RESULT_ERROR && data != null) {
            Throwable error = UCrop.getError(data);
            if (error != null) error.printStackTrace();
            toast("Crop canceled or failed.");
            return;
        }

        if (resultCode != RESULT_OK || data == null) return;

        Uri resultUri = UCrop.getOutput(data);
        if (resultUri == null) {
            toast("No image found.");
            return;
        }

        try {
            showLoading(true);
            Bitmap croppedBitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(resultUri)
            );

            bgRemover.process(croppedBitmap, new BGRemover.Callback() {
                @Override
                public void onSuccess(Bitmap cutoutBitmap) {
                    Uri imageUri = saveToInternalStorage(cutoutBitmap);
                    showLoading(false);

                    if (imageUri != null) {
                        Intent output = new Intent();
                        output.putExtra("image_uri", imageUri.toString());
                        setResult(RESULT_OK, output);
                        finish();
                    } else {
                        toast("Save failed after crop.");
                    }
                }

                @Override
                public void onError(Exception e) {
                    showLoading(false);
                    e.printStackTrace();
                    toast("Failed to process cropped image.");
                }
            });

        } catch (Exception e) {
            showLoading(false);
            e.printStackTrace();
            toast("Failed to process cropped image.");
        }
    }
}
