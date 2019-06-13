package jp.naklab.assu.android.android_myownfont;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jp.naklab.assu.android.android_myownfont.entity.Player;
import jp.naklab.assu.android.android_myownfont.svgandroid.SVG;
import jp.naklab.assu.android.android_myownfont.svgandroid.SVGBuilder;

public class MainActivity extends AppCompatActivity {
    TextView textView;
//    MainPresenter presenter;
    MainFontPresenter presenter;
    FontCanvas fontCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fontCanvas = findViewById(R.id.main_font_canvas);
        presenter = new MainFontPresenter(this);
        // これがメイン
        presenter.parseXML2String();
        initButtons();
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
    }


}
