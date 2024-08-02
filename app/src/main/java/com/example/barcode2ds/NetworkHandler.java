package com.example.barcode2ds;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class NetworkHandler {
    private static final OkHttpClient client = new OkHttpClient();

    public static void post(String url, FormBody formBody, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
