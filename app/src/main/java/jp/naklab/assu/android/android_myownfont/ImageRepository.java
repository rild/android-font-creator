package jp.naklab.assu.android.android_myownfont;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

// アプリで画像の保存・読み込みを行うクラス
public class ImageRepository {
    private HashMap<String, Uri> fontCacheUri;
    private Context context;

    public ImageRepository(Context context) {
        fontCacheUri = new HashMap<>();
        this.context = context;
    }

    public void saveImageBitmap(Bitmap bmp, String fontName, String uId) {
        try {
            String fileKey = fontName + uId;
            ByteArrayOutputStream byteArrOutputStream = new ByteArrayOutputStream();
            FileOutputStream fileOutputStream = context.openFileOutput(fileKey, Context.MODE_PRIVATE);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrOutputStream);
            fileOutputStream.write(byteArrOutputStream.toByteArray());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {

        }
    }

    public Bitmap loadImageBitmap(String fontName, String uId) {
        try {
            String fileKey = fontName + uId;
            BufferedInputStream bufferedInputStream = new BufferedInputStream(context.openFileInput(fileKey));
            return BitmapFactory.decodeStream(bufferedInputStream);
        } catch (FileNotFoundException e) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.background_white);
        }
    }

    public Bitmap loadBitmapFromUri(Uri bmpUri) {
        Bitmap bmp = null;

        if (bmpUri == null) {
            // hash map に存在しない→初めてロード
            bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.background_white);
            return bmp;
        }

        try {
            ParcelFileDescriptor parcelFileDesc = context.getContentResolver().openFileDescriptor(bmpUri, "r");
            FileDescriptor fDesc = parcelFileDesc.getFileDescriptor();
            bmp = BitmapFactory.decodeFileDescriptor(fDesc);
            parcelFileDesc.close();
            return bmp;
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
        return bmp;
    }

    public void save2ContentProvider(Bitmap bmp, String name) {
        if (fontCacheUri.get(name) == null) {
            String uriString = MediaStore.Images.Media.insertImage(context.getContentResolver(), bmp, name, null);
            fontCacheUri.put(name, Uri.parse(uriString));
        } else {
            // 既存のものを削除して登録し直す
            deleteFile(fontCacheUri.get(name));
            String uriString = MediaStore.Images.Media.insertImage(context.getContentResolver(), bmp, name, null);
            fontCacheUri.put(name, Uri.parse(uriString));
        }
    }

    public Bitmap loadFromContentProvider(String name) {
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
        for (String key : fontCacheUri.keySet()) {

        }
    }

    private void deleteFile(Uri uri) {
        File file = new File(uri.toString());
        if (file.exists()) {
            file.delete(); // ファイル自体の削除
            context.getContentResolver().delete(
                    MediaStore.Files.getContentUri("external"),
                    MediaStore.Files.FileColumns.DATA + "=?",
                    new String[]{uri.toString()}
            );  // アルバムからの削除
        }
    }
}
