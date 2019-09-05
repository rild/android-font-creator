package jp.naklab.assu.android.android_myownfont._;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainFontPresenter {

    final String TAG = "MainFontPresenter";
    Context context;

    public MainFontPresenter(Context context) {
        this.context = context;
    }

    // xml ファイルを読み取って、 textView に表示させる
    public void parseXML2String() {
        XmlPullParserFactory parserFactory;
        String xmlString = "";
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = context.getAssets().open("sample_svg_rialto.svg");
            xmlString = readTextFile(is);
        } catch (XmlPullParserException e) {

        } catch (IOException e) {
        }

        Log.d(TAG, xmlString);

        JSONObject jsonObj = null;
        try {
            jsonObj = XML.toJSONObject(xmlString);
        } catch (JSONException e) {
            Log.e("JSON exception", e.getMessage());
            e.printStackTrace();
        }

        //
        // Log.d(TAG, "xml:" + xmlString);

        try {
            String medtadata = (String) jsonObj.getJSONObject("svg").get("metadata");
            JSONObject font = jsonObj.getJSONObject("svg").getJSONObject("defs").getJSONObject("font");

            int vert_adv_y = font.getInt("vert-adv-y");

            JSONArray glyphArray = font.getJSONArray("glyph");
            Log.d(TAG, medtadata);
            Log.d(TAG, vert_adv_y + "");

            JSONObject glyph = (JSONObject) glyphArray.get(0);
            Log.d(TAG, glyph.getString("d"));

            glyph.put("d", "M10 20 L20 30");
            glyphArray.put(0, glyph);

        } catch (JSONException e) {

        }
    }



    private String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {


        }

        return outputStream.toString();

    }




}
