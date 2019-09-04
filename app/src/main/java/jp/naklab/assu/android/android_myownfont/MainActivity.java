package jp.naklab.assu.android.android_myownfont;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    //    MainPresenter presenter;
    MainFontPresenter presenter;
    FontCanvas fontCanvas;

    ImageView imageView;

//    static {
//        System.loadLibrary("opencv_java3");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6, API 23以上でパーミッシンの確認
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        } else {
            copyAssetsFile();
        }


        fontCanvas = findViewById(R.id.main_font_canvas);
        fontCanvas.setDrawingCacheEnabled(true);
        initButtons();

//        presenter = new MainFontPresenter(this);
//        // これがメイン
//        presenter.parseXML2String();
//
//        CloudConvert cloudConvert = new CloudConvert();
//        cloudConvert.function();

        if (!OpenCVLoader.initDebug()) {
            Log.i("OpenCV", "Failed");
        } else {
            Log.i("OpenCV", "successfully built !");
        }
    }

    public Bitmap loadBitmapFromView(View v) {
//        int w = v.getLayoutParams().width;
//        int h = v.getLayoutParams().height;
        int w = v.getWidth();
        int h = v.getHeight();
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    public @NonNull
    static Bitmap createBitmapFromView(@NonNull View view, int width, int height) {
        if (width > 0 && height > 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(width), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(height), View.MeasureSpec.EXACTLY));
        }

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return bitmap;
    }

    static Bitmap createBitmapFromView(@NonNull View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width > 0 && height > 0) {
            view.measure(View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(width), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(DynamicUnitUtils
                            .convertDpToPixels(height), View.MeasureSpec.EXACTLY));
        }

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return bitmap;
    }

    private void initButtons() {
        findViewById(R.id.button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fontCanvas.clear();
            }
        });
        findViewById(R.id.button_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fontCanvas.undo();
            }
        });
        final Button buttonMake = findViewById(R.id.button_make);
        buttonMake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenCVEdgeDetector edgeDetector = new OpenCVEdgeDetector();
                Bitmap bmp = fontCanvas.getDrawingCache();
//                Bitmap bmp = createBitmapFromView(fontCanvas, fontCanvas.getWidth(), fontCanvas.getHeight());
                Bitmap sizeTemplate = BitmapFactory.decodeResource(getResources(), R.drawable.background_green);
                bmp = Bitmap.createScaledBitmap(bmp, sizeTemplate.getWidth(), sizeTemplate.getHeight(), false);
//                Bitmap bmp2 = BitmapFactory.decodeResource(getResources(), R.drawable.equal_character_image_small);
                String glyphString = edgeDetector.makeGlyphString(bmp);
            }
        });
        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        final ImageView preview = findViewById(R.id.imageview_preview);
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preview.setBackground(new BitmapDrawable(fontCanvas.getDrawingCache()));
            }
        });
    }

    private void save() {
        String fileName = "myfont.jpg";
//        fileName = "Only_my_font/" + fileName;

        String filePath = Environment.getExternalStorageDirectory().getPath() + "/Pictures/";
        fileName = filePath + fileName;

        if (fontCanvas.getDrawingCache() == null) {   //save失敗
            Toast.makeText(getApplicationContext(), "何か記入してね！", Toast.LENGTH_SHORT).show();
            return;
        }

        String imgSaved = MediaStore.Images.Media.insertImage(
                getContentResolver(), fontCanvas.getDrawingCache(), fileName, "drawing");

        if (imgSaved != null) {
            Toast.makeText(getApplicationContext(), "Fontが保存されました！" + imgSaved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "保存に失敗しました", Toast.LENGTH_SHORT).show();
        }
    }

    // permissionの確認
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            copyAssetsFile();
        }
        // 拒否していた場合
        else {
            requestLocationPermission();
        }
    }

    private final int REQUEST_PERMISSION = 1000;

    // 許可を求める
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            Toast toast = Toast.makeText(this, "許可してください", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);
        }
    }


    // ファイルセパレータ
    public static final String SEPARATOR = File.separator;
    // コピー対象のファイル名
    private static final String FILE_NAME = "sample_svg_rialto.svg";
    // コピー先のディレクトリパス
    private static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath() + SEPARATOR + "fonts";

    private boolean copyAssetsFile() {
        // コピー先のディレクトリ
        File dir = new File(BASE_PATH);

        // コピー先のディレクトリが存在しない場合は生成
        if (!dir.exists()) {
            // ディレクトリの生成に失敗したら終了
            if (!dir.mkdirs()) {
                return false;
            }
        }

        // こぴーしょり
        try {
            InputStream inputStream = getAssets().open(FILE_NAME);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(BASE_PATH + SEPARATOR + FILE_NAME), false);

            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) >= 0) {
                fileOutputStream.write(buffer, 0, length);
            }

            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            // 何かテキトーに
            return false;
        }
        return true;
    }
}
