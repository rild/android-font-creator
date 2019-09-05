package jp.naklab.assu.android.android_myownfont;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    //    MainPresenter presenter;
//    MainFontPresenter presenter;
    FontCanvas fontCanvas;


    Spinner spinner;
    String currentItem;
    HashMap<String, Uri> fontCacheUri;

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


        spinner = findViewById(R.id.spinner_font_menu);
        fontCanvas = findViewById(R.id.main_font_canvas);
        fontCanvas.setDrawingCacheEnabled(true);
        initViews();
        currentItem = FontMaker.getUId(spinner.getSelectedItemPosition());

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

        fontCacheUri = new HashMap<>();

    }




    private void save2ContentProvider(Bitmap bitmap, String name) {
        if (fontCacheUri.get(name) == null) {
            String uriString = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, name, null);
            fontCacheUri.put(name, Uri.parse(uriString));
        }
    }

    private Bitmap loadFromContentProvider(String name) {
        Bitmap bmp = null;
        Uri bmpUri = fontCacheUri.get(name);

        if (bmpUri == null) {
            // hash map に存在しない→初めてロード
            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.background_white);
            return bmp;
        }
        try {
            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), bmpUri);
        } catch (IOException e) {
            // 何らかの理由で外部に書き出したファイルがなくなっていた場合は白地を出す
            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.background_white);
        }
        return bmp;
    }

    // TODO delete all images saved in this app from ContentProvider
    private void clearAllCaches() {
        for (String key: fontCacheUri.keySet()) {

        }
    }
    private void initViews() {
        findViewById(R.id.button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFontCanvas();
            }
        });
        findViewById(R.id.button_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fontCanvas.undo();
            }
        });
        Button buttonMake = findViewById(R.id.button_make);
        buttonMake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeFont();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!spinner.isFocusable()) {
                    spinner.setFocusable(true);
                    return;
                }
                Snackbar.make(spinner, "[" + spinner.getSelectedItem().toString() + "] " + currentItem + "→" + FontMaker.getUId(i),
                        Snackbar.LENGTH_SHORT).show();

                Bitmap bmp = fontCanvas.getDrawingCache();
                Bitmap sizeTemplate = BitmapFactory.decodeResource(getResources(), R.drawable.background_green);
                bmp = Bitmap.createScaledBitmap(bmp, sizeTemplate.getWidth(), sizeTemplate.getHeight(), false);

                save2ContentProvider(bmp, currentItem);
                clearFontCanvas();
                currentItem = FontMaker.getUId(i);
                fontCanvas.setBackground(new BitmapDrawable(loadFromContentProvider(currentItem)));

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner.setFocusable(false);

    }

    private void clearFontCanvas() {
        fontCanvas.destroyDrawingCache();
        fontCanvas.clear();
    }

    private void makeFont() {

//        ProgressDialog progressDialog = ProgressDialog.newInstance("フォントの書き出し中");
//        progressDialog.show(getSupportFragmentManager(), "Tag");

        // これが目的の svg string
        FontMaker maker = new FontMaker();
        for (int i = 0; i < FontMaker.getApplyFontSize() + 1; i++) {
            String fontId = FontMaker.getUId(i);
            Bitmap bmp = loadFromContentProvider(fontId);
            maker.addGlyph(bmp, fontId);
        }

        String svg = maker.makeFontSvg("only font");
        writeSvg(svg);
//        progressDialog.dismiss();
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
//  "/storage/emulated/0/fonts/sample_svg_rialto.svg";

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

    private static final String FILE_NAME_TMP = "sample.svg";
    private boolean writeSvg(String svgString) {
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
//            InputStream inputStream = getAssets().open(FILE_NAME);
            InputStream inputStream = new ByteArrayInputStream(svgString.getBytes("utf-8"));

            FileOutputStream fileOutputStream = new FileOutputStream(new File(BASE_PATH + SEPARATOR + FILE_NAME_TMP), false);

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
