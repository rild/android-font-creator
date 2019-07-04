package jp.naklab.assu.android.android_myownfont.services;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class CloudConvertServiceGenerator {
    // ここでベースURLを指定する

    public static final String API_BASE_URL =
            "https://api.cloudconvert.com/";
    // v1/convert?input=upload&outputformat=ttf&inputformat=svg&apikey=" + APIUtil.string + "&wait=true&download=inline

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder = new Retrofit.Builder().baseUrl(API_BASE_URL);

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }
}
