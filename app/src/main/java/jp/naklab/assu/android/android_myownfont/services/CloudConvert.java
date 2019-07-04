package jp.naklab.assu.android.android_myownfont.services;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CloudConvert {
    final String TAG = "CloudConvert";

    public void function() {
        CloudConvertService service = CloudConvertServiceGenerator.createService(CloudConvertService.class);

        // POSTする画像・音楽・動画等のファイル
        String filePath = "/storage/emulated/0/fonts/sample_svg_rialto.svg";
        File file = new File(filePath);

        if (!file.exists()) {
            Log.d(TAG, "not exists" + file.getPath());
            return;
        }


        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Call<ResponseBody> call = service.upload(body);
        call.enqueue(new Callback<ResponseBody>() {

            // ステータスコードが４００等エラーコード以外のとき呼ばれる
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                CloudConvert.this.onResponse(response);
            }

            // ステータスコードが４００等エラーコードのとき呼ばれる
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                CloudConvert.this.onFailure();
            }
        });
    }

    // レスポンスをもらった時の処理
    private void onResponse(Response<ResponseBody> response) {
        // 成功時の処理
        // response.body();でHTMLレスポンスのbodyタグ内が取れる
        Log.d(TAG, "succeeded");

        if(!response.isSuccessful()){
            Log.e(TAG, "Something's gone wrong");
            // TODO: show error message
            return;
        }
        DownloadFileAsyncTask downloadFileAsyncTask = new DownloadFileAsyncTask();
        downloadFileAsyncTask.execute(response.body().byteStream());
    }

    // レスポンスがもらえなかった時の処理
    private void onFailure() {
        // 失敗時の処理
        Log.d(TAG, "failed");
    }

    // レスポンスから ttf データを取り出して、.ttfファイルを書き出すクラス
    // https://gldraphael.com/blog/downloading-a-file-using-retrofit/
    private class DownloadFileAsyncTask extends AsyncTask<InputStream, Void, Boolean> {

        final String appDirectoryName = "fonts";
        final File imageRoot = new File(Environment.getExternalStorageDirectory().getPath(), appDirectoryName);
        final String filename = "sample_svg_rialto.ttf";

        @Override
        protected Boolean doInBackground(InputStream... params) {
            InputStream inputStream = params[0];
            File file = new File(imageRoot, filename);
            OutputStream output = null;
            try {
                output = new FileOutputStream(file);

                byte[] buffer = new byte[1024]; // or other buffer size
                int read;

                Log.d(TAG, "Attempting to write to: " + imageRoot + "/" + filename);
                while ((read = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    Log.v(TAG, "Writing to buffer to output stream.");
                }
                Log.d(TAG, "Flushing output stream.");
                output.flush();
                Log.d(TAG, "Output flushed.");
            } catch (IOException e) {
                Log.e(TAG, "IO Exception: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (output != null) {
                        output.close();
                        Log.d(TAG, "Output stream closed sucessfully.");
                    }
                    else{
                        Log.d(TAG, "Output stream is null");
                    }
                } catch (IOException e){
                    Log.e(TAG, "Couldn't close output stream: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            Log.d(TAG, "Download success: " + result);
            // TODO: show a snackbar or a toast
        }
    }
}
