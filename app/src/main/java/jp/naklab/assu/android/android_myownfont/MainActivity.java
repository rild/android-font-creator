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
import com.google.android.material.tabs.TabLayout;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;

import jp.naklab.assu.android.android_myownfont.services.CloudConvert;

public class MainActivity extends AppCompatActivity {
    FontCanvas fontCanvas;
    Button buttonMake;
    Button buttonConvert;

    Button buttonUndo;
    Button buttonRedo;
    Button buttonClear;

    TabLayout tabLayout;
    Spinner spinner;
    String currentUId;

    ImageRepository imageRepository;
    FontRepository fontRepository;

    String fontName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fontName = "only font";

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
        fontCanvas.setBackground(new BitmapDrawable(imageRepository.loadImageBitmap(fontName, currentUId)));
    }

    private void initViews() {
        // init views
        spinner = findViewById(R.id.spinner_font_menu);
        fontCanvas = findViewById(R.id.main_font_canvas);

        buttonClear = findViewById(R.id.button_clear);
        buttonUndo = findViewById(R.id.button_undo);
        buttonMake = findViewById(R.id.button_make);
        buttonConvert = findViewById(R.id.button_convert);

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
        buttonConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(spinner, "変換を開始します",
                        Snackbar.LENGTH_SHORT).show();
                CloudConvert cloudConvert = makeCloudConverter();
                cloudConvert.convert(FontRepository.TMP_FILE_PATH, fontName);
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

        initTabLayout();

    }

    private void initTabLayout() {
        tabLayout = findViewById(R.id.tablayout_fonts);

        String[] fontArray = getResources().getStringArray(R.array.string_array_font_menu);
        for(String f: fontArray) {
            TabLayout.Tab tab = tabLayout.newTab().setText(f);
            tabLayout.addTab(tab);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onFontItemSelected(tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private CloudConvert makeCloudConverter() {
        CloudConvert cloudConvert = new CloudConvert();
        cloudConvert.setListener(new CloudConvert.OnConvertFinishListener() {
            @Override
            public void onConvertComplete() {
                Snackbar.make(spinner, "フォントファイルを書き出しました",
                        Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onConvertFailure() {
                Snackbar.make(spinner, "失敗しました",
                        Snackbar.LENGTH_SHORT).show();
            }
        });
        return cloudConvert;
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
        imageRepository.saveImageBitmap(bmp, fontName, currentUId);

        clearFontCanvas();
        currentUId = FontMaker.getUId(position);
        fontCanvas.setBackground(new BitmapDrawable(imageRepository.loadImageBitmap(fontName, currentUId)));

        //TODO FIX 再帰呼び出しにならない？？
        spinner.setSelection(position);
        tabLayout.getTabAt(position).select();
    }

    private void clearFontCanvas() {
        fontCanvas.destroyDrawingCache();
        fontCanvas.clear();
        fontCanvas.setBackgroundResource(R.drawable.background_white);
    }

    private void makeFont() {
        FontMaker maker = new FontMaker();
        for (int i = 0; i < FontMaker.getApplyFontSize() + 1; i++) {
            String fontId = FontMaker.getUId(i);
            Bitmap bmp = imageRepository.loadImageBitmap(fontName, fontId);
            maker.addGlyph(bmp, fontId);
        }

        // これが目的の svg string
        String svg = maker.makeFontSvg(fontName);
        fontRepository.writeSvg(svg);

        Snackbar.make(spinner, "書き出しが完了しました",
                Snackbar.LENGTH_SHORT).show();
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
