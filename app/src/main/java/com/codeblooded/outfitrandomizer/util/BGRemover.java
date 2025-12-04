package com.codeblooded.outfitrandomizer.util;

import android.content.Context;
import android.graphics.Bitmap;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;

import java.nio.ByteBuffer;

public class BGRemover {
    public interface Callback {
        void onSuccess(Bitmap cutoutBM);
        void onError(Exception except);
    }

    private final Segmenter segmenter;
    public BGRemover(Context context) {
        SelfieSegmenterOptions options = new SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE).build();
        segmenter = Segmentation.getClient(options);
    }

    private Bitmap applyMask(Bitmap src, SegmentationMask mask) {
        int maskWidth = mask.getWidth(), maskHeight = mask.getHeight();
        Bitmap scaledSrc = Bitmap.createScaledBitmap(src, maskWidth, maskHeight, true);
        int size = maskWidth * maskHeight;
        int[] srcPixels = new int[size], outPixels = new int[size];

        scaledSrc.getPixels(srcPixels, 0, maskWidth, 0, 0, maskWidth, maskHeight);

        ByteBuffer buffer = mask.getBuffer();
        buffer.rewind();
        for (int i = 0; i < size; i++) {
            float foregroundProb = buffer.getFloat();
            int alpha = (int) (foregroundProb * 255);
            int rgb = srcPixels[i] & 0x00FFFFFF;
            outPixels[i] = (alpha << 24) | rgb;
        }

        Bitmap result = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888);
        result.setPixels(outPixels, 0, maskWidth, 0, 0, maskWidth, maskHeight);
        if (src.getWidth() != maskWidth || src.getHeight() != maskHeight) {
            result = Bitmap.createScaledBitmap(result, src.getWidth(), src.getHeight(), true);
        }

        return result;
    }

    public void process(Bitmap src, Callback callback) {
        InputImage image = InputImage.fromBitmap(src, 0);
        segmenter.process(image).addOnSuccessListener(mask -> {
            try {
                Bitmap cutout = applyMask(src, mask);
                callback.onSuccess(cutout);
            } catch (Exception except) {
                callback.onError(except);
            }
        }).addOnFailureListener(callback::onError);
    }
}
