package com.jt.medialearn1.activity.primary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.jt.medialearn1.R;

import java.io.File;

public class Lesson1Activity extends AppCompatActivity {

    File imageFile;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson1);
        imageFile = new File(getExternalFilesDir(null), "1.jpg");
        ResourceUtils.copyFileFromAssets("1.jpg", imageFile.getAbsolutePath());

        imageBitmap = ImageUtils.getBitmap(imageFile);

        //普通绘制
        ((ImageView) findViewById(R.id.image0)).setImageBitmap(imageBitmap);

        //surface绘制
        drawWithSurfaceView();

        //自定义view绘制
        MyImageView myImageView = findViewById(R.id.image2);
        myImageView.setBitmap(imageBitmap);
    }

    private void drawWithSurfaceView() {
        SurfaceView surfaceView = findViewById(R.id.image1);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);

                Canvas canvas = holder.lockCanvas();
                Bitmap bitmap = ImageUtils.compressBySampleSize(imageBitmap, ConvertUtils.dp2px(200),
                        ConvertUtils.dp2px(200), false);
                canvas.drawBitmap(bitmap, 0, 0, paint);
                holder.unlockCanvasAndPost(canvas);

            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }

    public static class MyImageView extends View {
        Paint paint;
        Bitmap bitmap;
        Bitmap newBitmap;

        public MyImageView(Context context) {
            this(context, null);
        }

        public MyImageView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            newBitmap = null;
            invalidate();
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (bitmap == null) {
                return;
            }
            if (newBitmap == null) {
                newBitmap = ImageUtils.compressBySampleSize(bitmap, getMeasuredWidth(), getMeasuredHeight(), false);
            }
            canvas.drawBitmap(newBitmap, 0, 0, paint);
        }
    }
}