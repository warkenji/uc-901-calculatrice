package com.gaswa.calculatrice.mode.handwritting_recognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.LinkedList;

public class HandWrittingRecognition extends SurfaceView implements SurfaceHolder.Callback {


    private HandWrittingRecognitionThread thread;
    public final static int BRUSH_SIZE_MIN = 16;
    public final static int BRUSH_SIZE_MAX = 32;
    public static int BRUSH_SIZE = BRUSH_SIZE_MIN;
    public static final int DEFAULT_COLOR = Color.BLACK; //Color.argb(255, 30, 144, 255);
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;
    private Path mPath;
    private PointF pos;
    private Paint mPaint;
    private LinkedList<FingerPath> paths;
    private LinkedList<FingerPath> forwardPaths;
    private int currentColor;
    private int backgroundColor;
    private int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint;
    FirebaseVisionImage image;
    FirebaseVisionTextRecognizer detector;
    private ResultatListener resultatListener;

    public HandWrittingRecognition(Context context)  {
        this(context, null);
    }

    public HandWrittingRecognition(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        paths = new LinkedList<>();
        forwardPaths = new LinkedList<>();
        backgroundColor = DEFAULT_BG_COLOR;
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
        resultatListener = null;

        image = null;

        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        mEmboss = new EmbossMaskFilter(new float[] {1, 1, 1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;

        this.setFocusable(true);

        this.getHolder().addCallback(this);
        pos = new PointF();
    }

    public void normalMode()
    {
        strokeWidth = BRUSH_SIZE_MIN;
        currentColor = Color.BLACK;
    }

    public void deleteMode()
    {
        strokeWidth = BRUSH_SIZE_MAX;
        currentColor = Color.WHITE;
    }

    public void update()  {
        if(image == null) {
            image = FirebaseVisionImage.fromBitmap(mBitmap);

            detector.processImage(image)
                    .addOnSuccessListener(firebaseVisionText -> {
                        if(resultatListener != null)
                        {
                            resultatListener.onResultReceived(firebaseVisionText.getText().trim());
                        }
                    })
                    .addOnFailureListener(e -> {
                        if(resultatListener != null)
                        {
                            resultatListener.onResultReceived("");
                        }
                    })
                    .addOnCompleteListener(task -> image = null);
        }
    }

    @Override
    public void draw(Canvas canvas)  {
        super.draw(canvas);
        canvas.save();
        mCanvas.drawColor(backgroundColor);

        for (FingerPath fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss)
                mPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                mPaint.setMaskFilter(mBlur);

            mCanvas.drawPath(fp.path, mPaint);

        }

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.thread = new HandWrittingRecognitionThread(this, holder);
        this.thread.setContinuer(true);
        this.thread.start();
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean fin = false;

        while(!fin) {
            try {
                this.thread.setContinuer(false);

                // Parent thread must wait until the end of GameThread.
                this.thread.join();
                fin = true;
            }catch(InterruptedException e)  {
                e.printStackTrace();
                fin = false;
            }
        }
    }

    public void previous()
    {
        FingerPath fingerPath = paths.pollLast();

        if(fingerPath != null)
        {
            forwardPaths.add(fingerPath);
        }
    }

    public void forward()
    {
        FingerPath fingerPath = forwardPaths.pollLast();

        if(fingerPath != null)
        {
            paths.add(fingerPath);
        }
    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
        paths.add(fp);
        forwardPaths.clear();

        mPath.reset();
        mPath.moveTo(x, y);
        pos.set(x, y);
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - pos.x);
        float dy = Math.abs(y - pos.y);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(pos.x, pos.y, (x + pos.x) / 2, (y + pos.y) / 2);
            pos.set(x, y);
        }
    }

    private void touchUp() {
        mPath.lineTo(pos.x, pos.y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                touchUp();
                invalidate();
                break;
        }

        return true;
    }

    public void setResultatListener(ResultatListener resultatListener) {
        this.resultatListener = resultatListener;
    }
}
