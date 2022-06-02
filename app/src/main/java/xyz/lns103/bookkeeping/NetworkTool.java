package xyz.lns103.bookkeeping;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Message;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetworkTool {

    public static void httpGet(String url,Handler handler){
        Message msg = new Message();
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: ");
                msg.obj = "network failure";
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String buffer = response.body().string();
                Log.e(TAG, "onResponse: " + buffer);
                msg.obj = buffer;
                handler.sendMessage(msg);
            }
        });
    }

    public static void httpPost(String url,String post,Handler handler){
        Message msg = new Message();
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        RequestBody requestBody = RequestBody.create(post,JSON);
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: ");
                msg.obj = "network failure";
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String buffer = response.body().string();
                Log.e(TAG, "onResponse: " + buffer);
                msg.obj = buffer;
                handler.sendMessage(msg);
            }
        });
    }

    public static void httpPut(String url,String post,Handler handler){
        Message msg = new Message();
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        RequestBody requestBody = RequestBody.create(post,JSON);
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .put(requestBody)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: ");
                msg.obj = "network failure";
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String buffer = response.body().string();
                Log.e(TAG, "onResponse: " + buffer);
                msg.obj = buffer;
                handler.sendMessage(msg);
            }
        });
    }

    public static void httpDelete(String url,Handler handler){
        Message msg = new Message();
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .delete()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: ");
                msg.obj = "network failure";
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String buffer = response.body().string();
                Log.e(TAG, "onResponse: " + buffer);
                msg.obj = buffer;
                handler.sendMessage(msg);
            }
        });
    }
}
