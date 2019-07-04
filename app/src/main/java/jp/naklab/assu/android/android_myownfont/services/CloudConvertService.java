package jp.naklab.assu.android.android_myownfont.services;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface CloudConvertService {
    @Multipart // PathはベースURLを抜いたものでOK
    @POST("v1/convert?input=upload&outputformat=ttf&inputformat=svg&apikey=" + APIUtil.string + "&wait=true&download=inline")
    Call<ResponseBody> upload(@Part MultipartBody.Part file);
}
