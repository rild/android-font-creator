package jp.naklab.assu.android.android_myownfont;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;
import java.util.HashMap;

// アプリで画像の保存・読み込みを行うクラス
public class ImageRepository {
    HashMap<String, Uri> fontCacheUri;

    public ImageRepository() {
        fontCacheUri = new HashMap<>();
    }
    public void save2ContentProvider(Context context,
                                      Bitmap bitmap, String name) {
        if (fontCacheUri.get(name) == null) {
            String uriString = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, name, null);
            fontCacheUri.put(name, Uri.parse(uriString));
        }
    }

    public Bitmap loadFromContentProvider(Context context,
                                           String name) {
        Bitmap bmp = null;
        Uri bmpUri = fontCacheUri.get(name);

        if (bmpUri == null) {
            // hash map に存在しない→初めてロード
            bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.background_white);
            return bmp;
        }
        try {
            bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), bmpUri);
        } catch (IOException e) {
            // 何らかの理由で外部に書き出したファイルがなくなっていた場合は白地を出す
            bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.background_white);
        }
        return bmp;
    }

    // TODO delete all images saved in this app from ContentProvider
    private void clearAllCaches() {
        for (String key: fontCacheUri.keySet()) {

        }
    }
}
