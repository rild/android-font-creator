package jp.naklab.assu.android.android_myownfont;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    FontCanvas fontCanvas;
    Button buttonMake;
    Button buttonUndo;
    Button buttonRedo;
    Button buttonClear;

    Spinner spinner;
    String currentUId;

    ImageRepository imageRepository;
    FontRepository fontRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fontRepository = new FontRepository(this);
        // 画像の保存・読み込みを行うために必要なプログラム
        imageRepository = new ImageRepository(this);

        // Android 6, API 23以上でパーミッシンの確認
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        } else {
            fontRepository.copyAssetsFile();
        }
        // OpenCV をアプリで使うために必要
        if (!OpenCVLoader.initDebug()) {
            Log.i("OpenCV", "Failed");
        } else {
            Log.i("OpenCV", "successfully built !");
        }

        initViews();

        currentUId = FontMaker.getUId(spinner.getSelectedItemPosition());

//        CloudConvert cloudConvert = new CloudConvert();
//        cloudConvert.function();


    }

    private void initViews() {
        // init views
        spinner = findViewById(R.id.spinner_font_menu);
        fontCanvas = findViewById(R.id.main_font_canvas);

        buttonClear = findViewById(R.id.button_clear);
        buttonUndo = findViewById(R.id.button_undo);
        buttonMake = findViewById(R.id.button_make);

        // init click listeners
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFontCanvas();
            }
        });
        buttonUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fontCanvas.undo();
            }
        });
        buttonMake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeFont();
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onFontItemSelected(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 書いた情報を取得するために DrawingCache を有効にしておく
        fontCanvas.setDrawingCacheEnabled(true);

        // スピナーの要素を選んだ時の処理が初回起動時に動かないようにする
        spinner.setFocusable(false);

    }

    private void onFontItemSelected(int position) {
        // スピナーの要素を選んだ時の処理が初回起動時に動かないようにする
        if (!spinner.isFocusable()) {
            spinner.setFocusable(true);
            return;
        }

        // 何から何に変更されたのかを通知する（別になくてもいい）
        Snackbar.make(spinner, "[" + spinner.getSelectedItem().toString() + "] " + currentUId + "→" + FontMaker.getUId(position),
                Snackbar.LENGTH_SHORT).show();

        Bitmap bmp = fontCanvas.getDrawingCache();
        // FontMaker の方で font-svg ファイルへ定義する vert-adv-y を 1000 としているので Bitmap のサイズも 1000 にリサイズする
        bmp = Bitmap.createScaledBitmap(bmp, 1000, 1000, false);

        imageRepository.save2ContentProvider(bmp, currentUId);
        clearFontCanvas();
        currentUId = FontMaker.getUId(position);
        fontCanvas.setBackground(new BitmapDrawable(imageRepository.loadFromContentProvider(currentUId)));
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
            Bitmap bmp = imageRepository.loadFromContentProvider(fontId);
            maker.addGlyph(bmp, fontId);
        }

        String svg = maker.makeFontSvg("only font");
        fontRepository.writeSvg(svg);

        Snackbar.make(spinner, "書き出しが完了しました",
                Snackbar.LENGTH_SHORT).show();
//        progressDialog.dismiss();
    }


    // permissionの確認
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            fontRepository.copyAssetsFile();
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
}
