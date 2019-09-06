package jp.naklab.assu.android.android_myownfont;

import android.content.Context;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FontRepository {
    // ファイルセパレータ
    public static final String SEPARATOR = File.separator;
    // コピー対象のファイル名
    private static final String FILE_NAME = "sample_svg_rialto.svg";
    // コピー先のディレクトリパス
    public static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath() + SEPARATOR + "fonts";
    public static final String FILE_NAME_TMP = "sample.svg";

    public static final String TMP_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + SEPARATOR + "fonts" + SEPARATOR + FILE_NAME_TMP;

//  "/storage/emulated/0/fonts/sample_svg_rialto.svg";

    private Context context;

    public FontRepository(Context context) {
        this.context = context;
    }

    public boolean copyAssetsFile() {
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
            InputStream inputStream = context.getAssets().open(FILE_NAME);
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

    public boolean writeSvg(String svgString) {
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
