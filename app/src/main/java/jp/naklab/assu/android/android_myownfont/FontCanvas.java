package jp.naklab.assu.android.android_myownfont;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import jp.naklab.assu.android.android_myownfont.svgandroid.SVG;
import jp.naklab.assu.android.android_myownfont.svgandroid.SVGBuilder;

public class FontCanvas extends View {
    final String TAG = "FontFanvas";
    final int PATH_NO_DATA = -1;


    String pathString;
    Path currentPath;
    List<Path> paths;
    int currentPathIndex;

    float preX, preY;
    int minDistance = 10;

    Context context;
    Paint paint;

    private void init() {
        pathString = "";
        currentPath = null;

        paths = new ArrayList<>();
        currentPathIndex = PATH_NO_DATA;

        paint = new Paint();
        paint.setColor(0xFF000000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(10);
    }

    public FontCanvas(Context context) {
        this(context, null);
    }


    public FontCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Path p: paths) {
            canvas.drawPath(p, paint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                paths.add(currentPath);
                currentPathIndex++;

                currentPath.moveTo(x, y);
//                preX = x;
//                preY = y;
//                pathString += pathData("M", x, y);


                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (distance(x, y, preX, preY) < minDistance) break;
                currentPath.lineTo(x, y);
//                preX = x;
//                preY = y;
//
//                pathString += pathData("L", x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (distance(x, y, preX, preY) < minDistance) break;

                currentPath.lineTo(x, y);
//                preX = x;
//                preY = y;
//
//                pathString += pathData("L", x, y);
                invalidate();
                break;
        }

        if (currentPathIndex != PATH_NO_DATA) {
            paths.set(currentPathIndex, currentPath);
        }

        Log.d(TAG, pathString);

        return true;
    }

    private float distance(float x, float y, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y- y2, 2));
    }

    private String pathData(String action, float x, float y) {
        return pathString += action +  x + " " +  y;
    }

    public void clear() {
        init();
        invalidate();
    }

    public void undo() {
        paths.remove(currentPathIndex);
        currentPathIndex--;
        invalidate();
    }

    public void exportCanvasAsString() {

    }
}
