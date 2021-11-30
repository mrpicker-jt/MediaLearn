package com.jt.medialearn1.activity.primary;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.jt.medialearn1.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Lesson4Activity extends AppCompatActivity {

    SurfaceView surfaceView;
    Camera camera;

    boolean started;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson4);
        surfaceView = findViewById(R.id.surface_view);
        imageView = findViewById(R.id.image_view);
        PermissionUtils.permission(Manifest.permission.CAMERA).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                camera = Camera.open();
                camera.setDisplayOrientation(90);
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPictureFormat(ImageFormat.JPEG);
                camera.setParameters(parameters);
                camera.setPreviewCallback((data, camera) -> {
                    long tag = imageView.getTag() == null ? 0 : (long) imageView.getTag();
                    long curTime = System.currentTimeMillis();
                    if (curTime - tag < 100) {
                        return;
                    }
                    imageView.setTag(curTime);
                    Camera.Size previewSize = camera.getParameters().getPreviewSize();
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0,0,previewSize.width,previewSize.height),100,stream);
                    Bitmap bitmap = ImageUtils.bytes2Bitmap(stream.toByteArray());
                    Bitmap rotate = ImageUtils.rotate(bitmap, 90, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
                    imageView.setImageBitmap(rotate);
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                startCamera(surfaceView.getHolder());
            }

            @Override
            public void onDenied() {

            }
        }).request();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                if (camera == null) {
                    return;
                }
                startCamera(holder);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                if (camera == null) {
                    return;
                }
                started = false;
                camera.setPreviewCallback(null);
                camera.release();
            }
        });
    }

    private void startCamera(SurfaceHolder holder) {
        started = true;
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}